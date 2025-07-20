package com.busviet;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    String username, role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNav = findViewById(R.id.bottom_nav);

        // Nhận dữ liệu từ MainActivity
        username = getIntent().getStringExtra("username");
        role = getIntent().getStringExtra("role");

        // Logic hiển thị menu theo role (chỉ 1 trong 2 hiển thị)
        if ("Admin".equals(role)) {
            bottomNav.getMenu().findItem(R.id.nav_ticket).setVisible(false);
            bottomNav.getMenu().findItem(R.id.nav_create_route).setTitle("Tạo tuyến");
        } else if ("Customer".equals(role)) {
            bottomNav.getMenu().findItem(R.id.nav_create_route).setTitle("Tìm tuyến");
        }

        // Fragment đầu tiên
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        bundle.putString("role", role);
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);
        loadFragment(homeFragment);

        // Điều hướng menu
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            Bundle args = new Bundle();
            args.putString("username", username);
            args.putString("role", role);

            int itemId = item.getItemId();

            if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else if (itemId == R.id.nav_buy) {
                fragment = new BuyTicketFragment();
            } else if (itemId == R.id.nav_routes) {
                fragment = new BusRoutesFragment();
            } else if (itemId == R.id.nav_ticket) {
                fragment = new TicketManagerFragment();
            } else if (itemId == R.id.nav_create_route && "Admin".equals(role)) {
                fragment = new CreateRouteFragment(); // mở fragment tạo tuyến
            } else if (itemId == R.id.nav_create_route && "Customer".equals(role)) {
                fragment = new MapRouteFragment(); // mở fragment tạo tuyến
            }


            if (fragment != null) {
                fragment.setArguments(args);
            }

            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
