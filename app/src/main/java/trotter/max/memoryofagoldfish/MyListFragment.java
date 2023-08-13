package trotter.max.memoryofagoldfish;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import java.util.List;

public class MyListFragment extends ListFragment {
    MyViewModel mViewModel;
    int mCurCheckPosition = 0;
    boolean mSingleActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(getActivity()).get(MyViewModel.class);

        final  Observer<List<Item>> itemObserver = new Observer<List<Item>>() {
            @Override
            public void onChanged(@Nullable final List<Item> items) {
                ItemAdapter itemAdapter = new ItemAdapter(getActivity(), mViewModel.getItems().getValue());
                setListAdapter(itemAdapter);
            }
        };
        mViewModel.getItems().observe(this, itemObserver);
    }

    void showContent(int index) {
        mCurCheckPosition = index;

        if (mSingleActivity) {
            getListView().setItemChecked(index, true);
            GridItemFragment content = (GridItemFragment) getFragmentManager()
                    .findFragmentById(R.id.content);
            if (content == null || content.getShownIndex() != index) {
                content = GridItemFragment.newInstance(index);

                FragmentTransaction ft = getFragmentManager()
                        .beginTransaction();
                ft.replace(R.id.content, content);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }
        } else {
            Intent intent = new Intent();
            intent.setClass(getActivity(), ItemActivity.class);
            intent.putExtra("index", index);
            startActivity(intent);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("curChoice", mCurCheckPosition);
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        mViewModel.selectItem(position);
        showContent(position);
    }
}
