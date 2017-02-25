package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v13.view.ViewCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.utils.ArticleUtility;

import static com.example.xyzreader.ui.ArticleListActivity.EXTRA_CURRENT_ALBUM_POSITION;
import static com.example.xyzreader.ui.ArticleListActivity.EXTRA_STARTING_ALBUM_POSITION;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor>, ImageLoaderHelper.Callbacks {
    private static final String TAG = "ArticleDetailActivity";
    private Cursor mCursor;
    private long mStartId;
    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private int mCurrentPosition;
    private int mStartingPosition;

    private CollapsingToolbarLayout collapseToolBar;
    private ImageView photoView;
    private String mSelectedImageUrl;
    private ImageLoaderHelper imgLoadHelper;

    private static final String STATE_CURRENT_PAGE_POSITION = "state_current_page_position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        mStartingPosition = getIntent().getIntExtra(EXTRA_STARTING_ALBUM_POSITION, 0);
        if (savedInstanceState == null) {
            mCurrentPosition = mStartingPosition;
        } else {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_PAGE_POSITION);
        }

        imgLoadHelper = ImageLoaderHelper.getInstance(this);

        getLoaderManager().initLoader(0, null, this);

        collapseToolBar = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        photoView = (ImageView) findViewById(R.id.photo);
        ViewCompat.setTransitionName(photoView, getString(R.string.transition_photo) + mCurrentPosition);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(mCurrentPosition);
        mPager.setPageMargin((int) TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                    mSelectedImageUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);

                    if (imgLoadHelper.getImageLoader().isCached(mSelectedImageUrl, 0, 0)) {
                        Bitmap bitmap = imgLoadHelper.getBitmap(ArticleUtility.getCacheKey(mSelectedImageUrl));
                        setBackdropImage(bitmap);
                    } else {
                        setBackdropDefaults();
                    }
                }
                mCurrentPosition = position;
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }
    }

    @Override
    public void finishAfterTransition() {
        Intent data = new Intent();
        data.putExtra(EXTRA_STARTING_ALBUM_POSITION, mStartingPosition);
        data.putExtra(EXTRA_CURRENT_ALBUM_POSITION, mCurrentPosition);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_PAGE_POSITION, mCurrentPosition);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }

        mSelectedImageUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
        ImageLoaderHelper.getInstance(ArticleDetailActivity.this).requestFrom(this).getImageLoader()
            .get(mSelectedImageUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                }

                @Override
                public void onErrorResponse(VolleyError volleyError) {

                }
            });

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    public void setBackdropImage(Bitmap bitmap) {
        if (bitmap != null) {
            Palette palette = Palette.from(bitmap).generate();
            photoView.setImageBitmap(bitmap);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(palette.getDarkVibrantColor(ContextCompat.getColor(this, android.R.color.transparent)));
            }
            collapseToolBar.setContentScrimColor(palette.getVibrantColor(ContextCompat.getColor(this, android.R.color.transparent)));
        } else {
            setBackdropDefaults();
        }
    }

    public void setBackdropDefaults() {
        photoView.setImageBitmap(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        }
        collapseToolBar.setContentScrimColor(ContextCompat.getColor(this, android.R.color.transparent));
    }

    //No longer needed
    public void retryImageLoading() {
        //retry
        Log.i(TAG, "Retrying again to load image");
        if (mSelectedImageUrl != null) {
            ImageLoaderHelper.getInstance(ArticleDetailActivity.this).requestFrom(this).getImageLoader()
                .get(mSelectedImageUrl, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        }
    }

    @Override
    public void onAddedToCache(String key, Bitmap bitmap) {
        if (ArticleUtility.getCacheKey(mSelectedImageUrl).equals(key)) {
            setBackdropImage(bitmap);
        }
    }

    @Override
    public void onGetFromCache(String key, Bitmap bitmap) {
        if (ArticleUtility.getCacheKey(mSelectedImageUrl).equals(key)) {
            photoView.setImageBitmap(bitmap);
        }
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
