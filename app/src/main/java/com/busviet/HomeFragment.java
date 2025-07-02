package com.busviet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView welcomeText = view.findViewById(R.id.tvWelcome);
        Bundle args = getArguments();

        if (args != null) {
            String username = args.getString("username", "bạn");
            welcomeText.setText("🎉 Chào mừng " + username + " đến với BusViet!");
        }

        return view;
    }
}
