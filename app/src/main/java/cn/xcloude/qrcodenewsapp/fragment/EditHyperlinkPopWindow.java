package cn.xcloude.qrcodenewsapp.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import butterknife.BindView;
import cn.xcloude.qrcodenewsapp.R;

/**
 * Created by Administrator on 2018/2/14.
 */

public class EditHyperlinkPopWindow extends PopupWindow {
    private View mPopView;
    private OnOkClickListener onOkClickListener;

    EditText etAddress,etDisplayText;

    private Button okBtn;
    private ImageView backBtn;

    public EditHyperlinkPopWindow(Context context) {
        super(context);
        init(context);
        setPopupWindow();
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        //绑定布局
        mPopView = inflater.inflate(R.layout.fragment_edit_hyperlink, null);

        okBtn = mPopView.findViewById(R.id.btn_ok);
        backBtn = mPopView.findViewById(R.id.iv_back);

        etDisplayText = mPopView.findViewById(R.id.et_display_text);
        etAddress = mPopView.findViewById(R.id.et_address);

    }

    @SuppressLint("InlinedApi")
    private void setPopupWindow() {
        this.setContentView(mPopView);// 设置View
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);// 设置弹出窗口的高
        this.setFocusable(true);// 设置弹出窗口可
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));// 设置背景透明
        //这句话，让pop覆盖在输入法上面
        this.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        //这句话，让pop自适应输入状态
        this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onOkClickListener != null) {
                    onOkClickListener.onHyperlinkOK(etAddress.getText().toString(), etDisplayText.getText().toString());
                }
            }
        });

        mPopView.setOnTouchListener(new View.OnTouchListener() {// 如果触摸位置在窗口外面则销毁

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = mPopView.findViewById(R.id.edit_hyperlink_pop).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

    public void setOnOkClickListener(OnOkClickListener onOkClickListener) {
        this.onOkClickListener = onOkClickListener;
    }

    public interface OnOkClickListener {
        void onHyperlinkOK(String address, String text);
    }
}
