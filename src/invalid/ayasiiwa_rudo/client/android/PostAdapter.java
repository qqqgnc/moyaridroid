package invalid.ayasiiwa_rudo.client.android;

import invalid.ayasiiwa_rudo.client.Post;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

class ViewSetting {
    public String name;
    public Typeface font = null;
    public int fontDelta = 0;
    public boolean visibility = true;

    public ViewSetting(String n) {
        name = n;
    }
}

class ViewHolder {
    TextView title;
    TextView author;
    TextView authorHead;
    TextView time;
    TextView quotation;
    TextView body;
}

class MyLinkMovementMethod extends LinkMovementMethod
{
    @Override
    public boolean onKeyDown(TextView widget, Spannable buffer, int keyCode, KeyEvent event) {
        try {
            return super.onKeyDown(widget, buffer, keyCode, event);
        } catch (ActivityNotFoundException e){
            Toast.makeText(widget.getContext(), "Invalid Link", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        try {
            return super.onTouchEvent(widget, buffer, event);
        } catch (ActivityNotFoundException e){
            Toast.makeText(widget.getContext(), "Invalid Link", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private static MyLinkMovementMethod sInstance;
    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new MyLinkMovementMethod();
        return sInstance;
    }
}

public class PostAdapter extends ArrayAdapter<Post> {
    public static final String VIEW_BODY = "body";
    public static final String VIEW_HEADER = "header";
    public static final String VIEW_QUOTATION = "quotation";

    public static final ViewSetting body = new ViewSetting(VIEW_BODY);
    public static final ViewSetting header = new ViewSetting(VIEW_HEADER);
    public static final ViewSetting quotation = new ViewSetting(VIEW_QUOTATION);

    private static float baseFontSize = 14f;
    public static float scaledDensity = 1f;

    private List<Post> posts = null;
    private LayoutInflater infla = null;

    public PostAdapter(Context context, int resource, List<Post> objects) {
        super(context, resource, objects);
        if (objects != null)
            posts = objects;
        else
            posts = new ArrayList<Post>(); // dummy
        infla = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        baseFontSize = getContext().getResources().getDimension(R.dimen.font_size) / scaledDensity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder = null;
        if (v == null) {
            v = infla.inflate(R.layout.list_entry, null);
            holder = new ViewHolder();
            holder.title = (TextView) v.findViewById(R.id.TextTitle);
            holder.author = (TextView) v.findViewById(R.id.TextAuthor);
            holder.time = (TextView) v.findViewById(R.id.TextTime);
            holder.authorHead = (TextView) v.findViewById(R.id.TextAutherHead);
            holder.quotation = (TextView) v.findViewById(R.id.TextQuote);
            holder.body = (TextView) v.findViewById(R.id.TextBody);
            holder.quotation.setMovementMethod(MyLinkMovementMethod.getInstance());
            holder.body.setMovementMethod(MyLinkMovementMethod.getInstance());
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Post p = posts.get(position);

        if (header.visibility) {
            v.findViewById(R.id.HeaderLayout).setVisibility(View.VISIBLE);
            holder.title.setText(Html.fromHtml(p.getTitle()));
            holder.author.setText(Html.fromHtml(p.getAuthor()));

            holder.time.setText(p.getPostTime());
            float size =  baseFontSize + (header.fontDelta * scaledDensity);

            holder.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            holder.author.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            holder.time.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            holder.authorHead.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            if (header.font != null) {
                holder.title.setTypeface(header.font);
                holder.author.setTypeface(header.font);
                holder.time.setTypeface(header.font);
                holder.authorHead.setTypeface(header.font);
            }
        } else {
            v.findViewById(R.id.HeaderLayout).setVisibility(View.GONE);
        }

        if (quotation.visibility) {
            String q = p.getQuotationText();
            if (q.length() == 0) {
                holder.quotation.setVisibility(View.GONE);
            } else {
                holder.quotation.setVisibility(View.VISIBLE);
                holder.quotation.setTextSize(TypedValue.COMPLEX_UNIT_SP, baseFontSize
                        + quotation.fontDelta * scaledDensity);
                if (quotation.font != null)
                    holder.quotation.setTypeface(quotation.font);
                CharSequence qb = p.getQuotationTextBuffer();
                if (qb == null) {
                    qb = Html.fromHtml(q);
                    p.setQuotationTextBuffer(qb);
                }
                holder.quotation.setText(qb);
            }
        } else {
            holder.quotation.setVisibility(View.GONE);
        }

        if (body.visibility) {
            holder.body.setVisibility(View.VISIBLE);
            holder.body.setTextSize(TypedValue.COMPLEX_UNIT_SP, baseFontSize + body.fontDelta * scaledDensity);
            if (body.font != null)
                holder.body.setTypeface(body.font);
            CharSequence bb = p.getBodyTextBuffer();
            if (bb == null) {
                bb = Html.fromHtml(p.getBodyText());
                p.setBodyTextBuffer(bb);
            }
            holder.body.setText(bb);
        } else {
            holder.body.setVisibility(View.GONE);
        }
        return v;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void addAll(Collection<? extends Post>  ps) {
        posts.addAll(ps);
    }

}
