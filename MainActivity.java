package test.jy.com.test2;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        initData();
    }

    private void initData() {

        TextView tvContent = (TextView) findViewById(R.id.tv_content);

        ArrayList<String> list = new ArrayList<>();
        for(int i = 0; i < 5 ; i++) {

            list.add(new String());
        }
        SpannableStringBuilder sb = new SpannableStringBuilder();

        for(String content : list) {

            int start = sb.length();
            String content1 = "qqqq" + " : " + "1121" + "\n";
            sb.append(content1);

            sb.setSpan(new MyClickableSpan(content1, true), start, start + 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            sb.setSpan(new MyClickableSpan(content1, false), start, start + content1.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        tvContent.setMovementMethod(MyLinkMovementMethod.getInstance());
//        tvContent.setHighlightColor(Color.parseColor("#36969696"));
        tvContent.setText(sb);

    }

    static class MyClickableSpan extends ClickableSpan {

        private String content;
        private boolean isChangeColor;
        private OnClickListener clickListener;

        public MyClickableSpan(String content, boolean isChangeColor) {
            this.content = content;
            this.isChangeColor = isChangeColor;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false);
            if(isChangeColor)
                ds.setColor(Color.parseColor("#0000ee"));
        }

        @Override
        public void onClick(View widget) {

                Toast.makeText(widget.getContext(), "点击", Toast.LENGTH_SHORT).show();
            if(clickListener != null)
                clickListener.onClick(widget);

        }

        public void onLongClick(View widget) {

                Toast.makeText(widget.getContext(), "长按", Toast.LENGTH_SHORT).show();
            if(clickListener != null)
                clickListener.onLongClick(widget);
        }


        public interface OnClickListener {
            void onLongClick(View widget);
            void onClick(View widget);
        }
    }

    static class MyLinkMovementMethod extends LinkMovementMethod {

        private static MyLinkMovementMethod sInstance;
        private Handler handler = new Handler(Looper.getMainLooper());
        private boolean doLongClick;
        private Timer timer ;

        @Override
        public boolean onTouchEvent(final TextView widget, final Spannable buffer,
                                    MotionEvent event) {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                final ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

                final Runnable longClikTask =  new Runnable() {
                    @Override
                    public void run() {

                        MyClickableSpan myClickableSpan = (MyClickableSpan) link[0];
                        myClickableSpan.onLongClick(widget);
                        Selection.removeSelection(buffer);
                        doLongClick = true;
                    }
                };

                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {

                        timer.cancel();
                        if(!doLongClick) {

                            link[0].onClick(widget);
                            Selection.removeSelection(buffer);
                        }
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));

                        doLongClick = false;
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                handler.post(longClikTask);
                            }
                        },1000);

                    }

                    return true;
                } else {
                    Selection.removeSelection(buffer);
                }
            }

            return super.onTouchEvent(widget, buffer, event);
        }

        public static MovementMethod getInstance() {
            if (sInstance == null)
                sInstance = new MyLinkMovementMethod();

            return sInstance;
        }

    }
}
