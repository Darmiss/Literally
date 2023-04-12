package com.cjcj55.literallynot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.cjcj55.literallynot.databinding.FragmentScoreboardBinding;
import com.cjcj55.literallynot.db.LBEntry;
import com.cjcj55.literallynot.db.LeaderboardCallback;
import com.cjcj55.literallynot.db.MySQLHelper;

import java.util.List;


public class scoreboard extends Fragment {

    private FragmentScoreboardBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentScoreboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        MySQLHelper.getLeaderboard(getContext(), new LeaderboardCallback() {
            @Override
            public void onSuccess(List<LBEntry> leaderboard) {
                int length = leaderboard.size();

                if (length >= 1) {
                    TextView textView1 = view.findViewById(R.id.pos1);
                    textView1.setText(leaderboard.get(0).toString());
                }

                if (length >= 2) {
                    TextView textView2 = view.findViewById(R.id.pos2);
                    textView2.setText(leaderboard.get(1).toString());
                }

                if (length >= 3) {
                    TextView textView3 = view.findViewById(R.id.pos3);
                    textView3.setText(leaderboard.get(2).toString());
                }

                if (length >= 4) {
                    TextView textView4 = view.findViewById(R.id.pos4);
                    textView4.setText(leaderboard.get(3).toString());
                }

                if (length >= 5) {
                    TextView textView5 = view.findViewById(R.id.pos5);
                    textView5.setText(leaderboard.get(4).toString());
                }

                if (length >= 6) {
                    TextView textView6 = view.findViewById(R.id.pos6);
                    textView6.setText(leaderboard.get(5).toString());
                }

                if (length >= 7) {
                    TextView textView7 = view.findViewById(R.id.pos7);
                    textView7.setText(leaderboard.get(6).toString());
                }

                if (length >= 8) {
                    TextView textView8 = view.findViewById(R.id.pos8);
                    textView8.setText(leaderboard.get(7).toString());
                }

                if (length >= 9) {
                    TextView textView9 = view.findViewById(R.id.pos9);
                    textView9.setText(leaderboard.get(8).toString());
                }

                if (length >= 10) {
                    TextView textView10 = view.findViewById(R.id.pos10);
                    textView10.setText(leaderboard.get(9).toString());
                }
            }

            @Override
            public void onFailure() {

            }
        });

        binding.homebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(scoreboard.this)
                        .navigate(R.id.action_scoreboard_to_MainScreen);
            }
        });
        binding.reportbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(scoreboard.this)
                        .navigate(R.id.action_scoreboard_to_ReportScreen);
            }
        });

        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(scoreboard.this)
                        .navigate(R.id.action_scoreboard_to_accountMenu);
            }
        });
    }
}