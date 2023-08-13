package trotter.max.memoryofagoldfish;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Collections;

public class GridItemFragment extends Fragment {

    private static final String ARG_INDEX = "index";

    // TODO: Rename and change types of parameters
    private int mIndex;
    MyViewModel mViewModel;
    View mInflatedView;

    public int getShownIndex() {
        return mIndex;
    }

    public GridItemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param index Parameter 1 is the index of the content data we want to display.
     * @return A new instance of fragment ListItemFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GridItemFragment newInstance(int index) {
        GridItemFragment fragment = new GridItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIndex = getArguments().getInt(ARG_INDEX);
        }
        mViewModel = new ViewModelProvider(getActivity()).get(MyViewModel.class);
        mViewModel.selectItem(mIndex);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i(this.getClass().getSimpleName() + " Observer", "onCreateView");
        mInflatedView = inflater.inflate(R.layout.fragment_grid, container, false);
        GridLayout gridLayout = mInflatedView.findViewById(R.id.gridLayout);
        ArrayList<View> tileViews = new ArrayList<View>();
        for(int i = 0; i < gridLayout.getChildCount(); i++){
            tileViews.add(gridLayout.getChildAt(i));
        }
        Collections.shuffle(tileViews);
        final Observer<Item> itemObserver = new Observer<Item>() {
            @Override
            public void onChanged(@Nullable final Item item) {
                for(int i = 0; i < tileViews.size(); i++){
                    View subView = tileViews.get(i);
                    if(subView instanceof ImageView){
                        ImageView imageView = (ImageView) subView;
                        imageView.setImageBitmap(item.getTileBackImage());
                        int finalI = i;
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                imageView.setImageBitmap(item.getImages().get(finalI));
                            }
                        });
                        }
                    }
                }
        };
        mViewModel.getSelectedItem().observe(getViewLifecycleOwner(), itemObserver);
        return mInflatedView;
    }
}