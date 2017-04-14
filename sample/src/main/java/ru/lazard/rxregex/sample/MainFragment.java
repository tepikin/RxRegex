package ru.lazard.rxregex.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.jakewharton.rxbinding2.widget.RxTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.lazard.rxregex.RxRegex;


public class MainFragment extends Fragment {

    public static final int DELAY = 1000;
    @BindView(R.id.regularExpression)
    EditText mRegularExpressionView;
    @BindView(R.id.replacement)
    EditText mReplacementView;
    @BindView(R.id.textForTest)
    EditText mTestView;
    @BindView(R.id.result)
    EditText mResultView;
    @BindView(R.id.fakeDelay)
    CheckBox mFakeDelay;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        startSubscriptions();
        mRegularExpressionView.setText("1(23)4");
        mReplacementView.setText("_A$1D_");
        mTestView.setText("Text for test regex 1234 abcd +7(900) 1234-5678");
        return view;
    }

    // I must write all logic in one Rx sequence (i don't wont, but i must). So, here it is.
    private void startSubscriptions() {
        Observable.merge(
                RxTextView.afterTextChangeEvents(mRegularExpressionView).distinctUntilChanged(event -> event.editable().toString()),
                RxTextView.afterTextChangeEvents(mReplacementView).distinctUntilChanged(event -> event.editable().toString()),
                RxTextView.afterTextChangeEvents(mTestView).distinctUntilChanged(event -> event.editable().toString())
        ).map(event -> {
            for (BackgroundColorSpan backgroundColorSpan : mTestView.getEditableText().getSpans(0, mTestView.getEditableText().length(), BackgroundColorSpan.class)) {
                mTestView.getEditableText().removeSpan(backgroundColorSpan);
            }
            mTestView.invalidate();
            mResultView.setText("");
            mRegularExpressionView.setError(null);
            return RxRegex.replace(mTestView.getText().toString(), mRegularExpressionView.getText().toString(), mReplacementView.getText().toString())
                    .doOnNext(dualSpan -> Thread.sleep(mFakeDelay.isChecked() ? DELAY : 0))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .scan(new DualSpan(), (dualSpan, onAppend) -> dualSpan.append(onAppend)).skip(1)
                    .onErrorReturn(throwable -> {
                        mRegularExpressionView.setError(throwable.toString());
                        return new DualSpan();
                    })
                    .doOnNext(dualSpan -> mResultView.setText(dualSpan.dstSpannable))
                    .doOnNext(dualSpan -> mTestView.invalidate())
                    .subscribe();
        }).scan((disposable, disposable2) -> {
            disposable.dispose();
            return disposable2;
        }).subscribe();
    }


    private class DualSpan {
        private SpannableStringBuilder dstSpannable = new SpannableStringBuilder();

        public DualSpan append(RxRegex.OnAppend onAppend) {
            dstSpannable.append(onAppend.getAppendDst());
            if (onAppend.isMatched()) {
                mTestView.getText().setSpan(new BackgroundColorSpan(Color.LTGRAY), onAppend.getFromSrc(), onAppend.getToSrc(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                dstSpannable.setSpan(new BackgroundColorSpan(Color.LTGRAY), onAppend.getFromDst(), onAppend.getToDst(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return this;
        }
    }

}
