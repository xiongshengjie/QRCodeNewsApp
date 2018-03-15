package cn.xcloude.qrcodenewsapp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.xcloude.qrcodenewsapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListNewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListNewsFragment extends Fragment {

    private static final String categoryId = "categoryId";
    private static final String categoryName = "categoryName";

    private int id;
    private String name;

    public ListNewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param id   分类id.
     * @param name 分类名称.
     * @return A new instance of fragment ListNewsFragment.
     */
    public static ListNewsFragment newInstance(int id, String name) {
        ListNewsFragment fragment = new ListNewsFragment();
        Bundle args = new Bundle();
        args.putInt(categoryId, id);
        args.putString(categoryName, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getInt(categoryId);
            name = getArguments().getString(categoryName);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_news, container, false);
        TextView text = rootView.findViewById(R.id.category);
        text.setText(name+id);
        return rootView;
    }
}
