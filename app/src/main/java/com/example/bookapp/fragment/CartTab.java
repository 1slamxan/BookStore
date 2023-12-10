package com.example.bookapp.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.bookapp.activities.PaymentDetails;
import com.example.bookapp.adapter.AdapterCart;
import com.example.bookapp.databinding.FragmentCartTabBinding;
import com.example.bookapp.model.ModelPdf;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.stripe.model.tax.Registration;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CartTab extends Fragment {
    private static final String YOUR_CLIENT_ID = "AX_KF-zzH3tmmFpjt62_ZRyWtQ4JTuZPlUaogTMKGZ8AHvT7fk1xZx8fb6fG8m0z186tKI1x2sa2saqY";
    int PAYPAL_REQUEST_CODE = 123;
    private ArrayList<ModelPdf> pdfArrayList;
    private FirebaseAuth firebaseAuth;
    private AdapterCart adapterCart;
    private FragmentCartTabBinding binding;
    private double total;
    private String bookId;

    private String PublishableKey = "pk_test_51OJnfOIpK9UzeYIlwk6k3edFQ1760ORWrjYIvYgdbiZ8cwCcbkPYoDHRLdzGqnVfNJ5PTwjxgKH4SUL11JbBRCz900xJXqERps";
    private String SecretKey = "sk_test_51OJnfOIpK9UzeYIldtDTFfIym7FILV9m6cfxnGYdumhFSRy7ruLx3rvSeQRkHx2CxsWu0Ci2Sc7kNqTl8T1C42F60031SSx97g";
    private String CustomerId;
    private String EphemeralKey;
    private String ClientSecret;
    private PaymentSheet paymentSheet;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        binding = FragmentCartTabBinding.inflate(LayoutInflater.from(getContext()), container, false);
        loadBooksInCart();
        PaymentConfiguration.init(getContext(), PublishableKey);

        paymentSheet = new PaymentSheet(this, paymentSheetResult -> {
            onPaymentResult(paymentSheetResult);
        });


        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/customers",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        CustomerId = object.getString("id");
                        Toast.makeText(getContext(), CustomerId, Toast.LENGTH_SHORT).show();
                        getEphemeralKey();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getContext(), ""+error.getLocalizedMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                return header;

            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(request);

        binding.buyBtn.setOnClickListener(v -> {
            paymentFlow();
        });
        return binding.getRoot();
    }

    private void paymentFlow() {
        paymentSheet.presentWithPaymentIntent(ClientSecret, new PaymentSheet.Configuration("Книжгый Магазин", new PaymentSheet.CustomerConfiguration(
                CustomerId, EphemeralKey
        )));
    }

    private void onPaymentResult(PaymentSheetResult paymentSheet) {

        if (paymentSheet instanceof PaymentSheetResult.Completed) {
            addBoughtBooksToDb();
        }
    }

    private void getEphemeralKey() {

        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/ephemeral_keys",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        EphemeralKey = object.getString("secret");
                        Toast.makeText(getContext(), EphemeralKey, Toast.LENGTH_SHORT).show();
                        getClientSecret(CustomerId, EphemeralKey);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getContext(), ""+error.getLocalizedMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                header.put("Stripe-Version", "2023-10-16");
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);

                return params;

            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(request);
    }

    private void getClientSecret(String customerId, String ephemeralKey) {

        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        ClientSecret = object.getString("client_secret");
                        Toast.makeText(getContext(), ClientSecret, Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getContext(), ""+error.getLocalizedMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerId);
                params.put("amount", "1099");
                params.put("currency", "USD");
                params.put("automatic_payment_methods[enabled]", "true");

                return params;

            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(request);
    }


    private void addBoughtBooksToDb() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Вы не вошли в систему", Toast.LENGTH_SHORT).show();
        } else {
            long timestamp = System.currentTimeMillis();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId", bookId);
            hashMap.put("timestamp", "" + timestamp);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Buys").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener(unused -> {
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
                        reference1.child(firebaseAuth.getUid()).child("Cart").child(bookId).removeValue();
                        Toast.makeText(getContext(), "Покупка прошла успешно", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Не удалось купить товар из-за " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadBooksInCart() {
        pdfArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Cart")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            bookId = "" + ds.child("bookId").getValue();

                            ModelPdf modelPdf = new ModelPdf();
                            modelPdf.setId(bookId);

                            pdfArrayList.add(modelPdf);
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                            ref.child(bookId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String price = snapshot.child("price").getValue(String.class);
                                            total += Double.parseDouble(price);
                                            binding.totalTv.setText("" + total);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }

                        adapterCart = new AdapterCart(getContext(), pdfArrayList);
                        binding.booksRV.setAdapter(adapterCart);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
