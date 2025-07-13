package com.busviet;

import android.app.Activity; // ✅ Thêm import này
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup; // ✅ Thêm import này
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BusRoutesFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    EditText etStart, etEnd;
    Button btnFindRoute;
    RecyclerView recyclerView;
    BusRouteAdapter adapter;
    List<BusRoute> busRouteList = new ArrayList<>();

    private static final int AUTOCOMPLETE_REQUEST_CODE_START = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE_END = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bus_routes, container, false);

        // 1. Find View trước
        etStart = view.findViewById(R.id.etStart);
        etEnd = view.findViewById(R.id.etEnd);
        btnFindRoute = view.findViewById(R.id.btnFindRoute);
        recyclerView = view.findViewById(R.id.recyclerView);

        // 2. Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BusRouteAdapter(busRouteList, (route, polyline) -> showRouteDetail(route, polyline));
        recyclerView.setAdapter(adapter);

        // 3. Hiển thị Empty View nếu danh sách trống
        updateEmptyViewVisibility(); // 👈 Gọn gàng

        // 4. Khởi tạo Places nếu chưa
        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), "AIzaSyDodq43WJMoePSko6uJ8hh6kqYXfDONJXI");  // TODO: Replace with real API Key
        }

        // 5. Setup bản đồ
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 6. Xử lý click autocomplete
        etStart.setOnClickListener(v -> openAutocomplete(AUTOCOMPLETE_REQUEST_CODE_START));
        etEnd.setOnClickListener(v -> openAutocomplete(AUTOCOMPLETE_REQUEST_CODE_END));

        // 7. Xử lý nút Tìm tuyến
        btnFindRoute.setOnClickListener(v -> {
            String start = etStart.getTag() != null ? etStart.getTag().toString() : "";
            String end = etEnd.getTag() != null ? etEnd.getTag().toString() : "";

            if (!start.isEmpty() && !end.isEmpty()) {
                callDirectionsAPI(start, end);
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn địa chỉ từ gợi ý", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // ✅ Hàm hỗ trợ gọn gàng, dễ gọi lại
    private void updateEmptyViewVisibility() {
        // Kiểm tra null để tránh crash
        if (getView() == null) return;
        
        View emptyView = getView().findViewById(R.id.emptyView);
        if (emptyView == null) return; // Kiểm tra layout có tồn tại không
        
        if (busRouteList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }


    private void openAutocomplete(int requestCode) {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(getActivity());
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE_START || requestCode == AUTOCOMPLETE_REQUEST_CODE_END) {
            if (resultCode == Activity.RESULT_OK && data != null) { // ✅ Thay đổi này
                Place place = Autocomplete.getPlaceFromIntent(data);
                String latLngStr = place.getLatLng().latitude + "," + place.getLatLng().longitude;

                if (requestCode == AUTOCOMPLETE_REQUEST_CODE_START) {
                    etStart.setText(place.getName());
                    etStart.setTag(latLngStr);
                } else {
                    etEnd.setText(place.getName());
                    etEnd.setTag(latLngStr);
                }

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR && data != null) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.e("AutocompleteError", status.getStatusMessage());
                Toast.makeText(getContext(), "Lỗi gợi ý: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();

            } else if (resultCode == Activity.RESULT_CANCELED) { // ✅ Thay đổi này
                // Người dùng huỷ hoặc thoát autocomplete
                Log.d("Autocomplete", "User cancelled autocomplete");
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng hcm = new LatLng(10.762622, 106.660172);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcm, 15));
    }

    private void callDirectionsAPI(String start, String end) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DirectionsService service = retrofit.create(DirectionsService.class);

        Call<DirectionsResponse> call = service.getDirections(
                start,
                end,
                "transit",
                "now",
                "AIzaSyDodq43WJMoePSko6uJ8hh6kqYXfDONJXI"
        );

        call.enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getRoutes().isEmpty()) {
                    List<LatLng> fullPath = new ArrayList<>();
                    busRouteList.clear();

                    DirectionsResponse.Route route = response.body().getRoutes().get(0);
                    if (route.getLegs().isEmpty()) {
                        showEmptyResult();
                        return;
                    }

                    DirectionsResponse.Leg leg = route.getLegs().get(0);

                    for (DirectionsResponse.Step step : leg.getSteps()) {
                        List<LatLng> stepPolyline = new ArrayList<>();

                        if (step.getPolyline() != null) {
                            stepPolyline = decodePolyline(step.getPolyline().getPoints());
                            fullPath.addAll(stepPolyline);
                        }

                        if (step.getTransitDetails() != null) {
                            String busNum = step.getTransitDetails().getLine().getShortName();
                            String depStop = step.getTransitDetails().getDepartureStop().getName();
                            String arrStop = step.getTransitDetails().getArrivalStop().getName();
                            int numStops = step.getTransitDetails().getNumStops();

                            busRouteList.add(new BusRoute(busNum, depStop, arrStop, numStops, stepPolyline));
                        }
                    }

                    // ✅ Sử dụng method an toàn
                    updateRecyclerView();
                    drawPolyline(fullPath);
                } else {
                    showEmptyResult();
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                showEmptyResult();
                Toast.makeText(getContext(), "Lỗi kết nối API", Toast.LENGTH_SHORT).show();
                Log.e("API", "Call failed: " + t.getMessage());
            }
        });
    }

    // ✅ Method mới để update RecyclerView an toàn
    private void updateRecyclerView() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateEmptyViewVisibility();
    }

    // ✅ Method mới để hiển thị kết quả trống
    private void showEmptyResult() {
        busRouteList.clear();
        updateRecyclerView();
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do { b = encoded.charAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5; } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0; result = 0;
            do { b = encoded.charAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5; } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }
        return poly;
    }

    private void drawPolyline(List<LatLng> path) {
        if (mMap == null) return;
        mMap.clear();
        mMap.addPolyline(new PolylineOptions()
                .addAll(path)
                .width(10)
                .color(Color.BLUE));

        if (!path.isEmpty()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(path.get(0), 14));
        }
    }

    private void showRouteDetail(BusRoute route, List<LatLng> polyline) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_route, null);

        TextView tvBusName = view.findViewById(R.id.tvBusName);
        TextView tvRoute = view.findViewById(R.id.tvRoute);
        TextView tvStops = view.findViewById(R.id.tvStops);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        Button btnShowMap = view.findViewById(R.id.btnShowMap);

        tvBusName.setText("Bus: " + route.getBusNumber());
        tvRoute.setText(route.getDepartureStop() + " → " + route.getArrivalStop());
        tvStops.setText("Số trạm: " + route.getNumStops());

        btnShowMap.setOnClickListener(v -> {
            drawPolyline(polyline);
            dialog.dismiss();
        });
        dialog.setContentView(view);
        dialog.show();
    }
}

