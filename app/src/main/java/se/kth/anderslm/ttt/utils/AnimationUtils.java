package se.kth.anderslm.ttt.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public class AnimationUtils {

    public static void fadeInAndOutImageView(View viewToFadeIn, ImageView imageView) {
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0.9F, 1.0F);
        alphaAnimator.setDuration(200);
        alphaAnimator.setInterpolator(new LinearInterpolator());

        alphaAnimator.addUpdateListener(valueAnimator -> {
            float newAlpha = (float) valueAnimator.getAnimatedValue();
            viewToFadeIn.setAlpha(newAlpha);
        });
        alphaAnimator.start();

            alphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    alphaAnimator.reverse();
                    alphaAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            imageView.setImageDrawable(null);
                            alphaAnimator.removeAllListeners();
                        }
                    });
                }
            });
    }
}
