package com.example.xyzreader.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.view.ViewCompat;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.widgets.ScaledImageView;
import com.example.xyzreader.widgets.TransitionAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
    LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private ScaledImageView mHeaderImageView;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private Typeface roboto_regular;
    private Typeface roboto_bold;
    private static final String ARG_ALBUM_IMAGE_POSITION = "arg_album_image_position";
    private static final String ARG_STARTING_ALBUM_IMAGE_POSITION = "arg_starting_album_image_position";
    private int mStartingPosition;
    private int mAlbumPosition;
    private FloatingActionButton fab;

    private final Callback mImageCallback = new Callback() {
        @Override
        public void onSuccess() {
            startPostponedEnterTransition();
        }

        @Override
        public void onError() {
            startPostponedEnterTransition();
        }
    };


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId, int position, int startingPosition) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        arguments.putInt(ARG_ALBUM_IMAGE_POSITION, position);
        arguments.putInt(ARG_STARTING_ALBUM_IMAGE_POSITION, startingPosition);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mItemId = getArguments().getLong(ARG_ITEM_ID);
        mStartingPosition = getArguments().getInt(ARG_STARTING_ALBUM_IMAGE_POSITION);
        mAlbumPosition = getArguments().getInt(ARG_ALBUM_IMAGE_POSITION);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        getCurrentActivity().setSupportActionBar(toolbar);

        mHeaderImageView = (ScaledImageView) mRootView.findViewById(R.id.header_image);
        ViewCompat.setTransitionName(mHeaderImageView, getString(R.string.transition_photo) + mAlbumPosition);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                    .setType("text/plain")
                    .setText("Some sample text")
                    .getIntent(), getString(R.string.action_share)));
            }
        });

        mCollapsingToolbar = (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsing_toolbar);

        fab = (FloatingActionButton) mRootView.findViewById(R.id.share_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                    .setType("text/plain")
                    .setText("Some sample text")
                    .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
        return mRootView;
    }

    public void onBackPressed() {
        fab.animate().scaleX(0).scaleY(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //FIXME not sure how to fix this. supportFinishAfterTransition() executes a reverse exit transaction as I have used a enter transition.
                //And since the FAB is anchored to app bar layout(sliding from top), FAB is also sliding from top during Enter transition.
                //During exit transaction its sliding back reverse back to top which forms a glitch in UI. Need help resolving this.
                getCurrentActivity().supportFinishAfterTransition();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        roboto_regular = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf");
        roboto_bold = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Bold.ttf");
    }

    private void startPostponedEnterTransition() {
        if (mAlbumPosition == mStartingPosition) {
            mHeaderImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mHeaderImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getActivity().startPostponedEnterTransition();
                    }
                    return true;
                }
            });
        }
    }

    private AppCompatActivity getCurrentActivity() {
        return ((AppCompatActivity) getActivity());
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setTypeface(roboto_bold);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        bodyView.setTypeface(roboto_regular);

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            mCollapsingToolbar.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));

            String subtitle = String.format(
                getString(R.string.author_line),
                DateUtils.getRelativeTimeSpanString(
                    mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString(),
                mCursor.getString(ArticleLoader.Query.AUTHOR));
            bylineView.setText(subtitle);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY), Html.FROM_HTML_MODE_LEGACY));
            } else {
                bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
            }
            bodyView.setMovementMethod(LinkMovementMethod.getInstance());

            String url = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            RequestCreator albumImageRequest = Picasso.with(getActivity()).load(url);
            albumImageRequest.into(mHeaderImageView, mImageCallback);
            mHeaderImageView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    @Nullable
    ImageView getAlbumImage() {
        if (isViewInBounds(getActivity().getWindow().getDecorView(), mHeaderImageView)) {
            return mHeaderImageView;
        }
        return null;
    }

    /**
     * Returns true if {@param view} is contained within {@param container}'s bounds.
     */
    private static boolean isViewInBounds(@NonNull View container, @NonNull View view) {
        Rect containerBounds = new Rect();
        container.getHitRect(containerBounds);
        return view.getLocalVisibleRect(containerBounds);
    }
}
