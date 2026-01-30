package app.capgo.capacitor.compass;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

public class AccuracyMonitor {

    private static final int RELATIVE_LAYOUT_ID = 1000;
    private static final int DEFAULT_HORIZONTAL_MARGIN = 50;
    private static final int DEFAULT_VERTICAL_MARGIN = 100;
    private static final String CALIBRATION_IMAGE_NAME = "calibration";
    private static final String DIALOG_TITLE = "Compass calibration required";
    private static final String CALIBRATION_HINT = "Tilt and move your phone 3 times in a figure-of-eight motion like this.";

    private Activity activity;
    private AlertDialog dialog = null;
    private TextView accuracyTextView = null;
    private boolean hasShownDialog = false;
    private int requiredAccuracy = 3; // Default to HIGH

    public AccuracyMonitor(Activity activity) {
        this.activity = activity;
    }

    public void setRequiredAccuracy(int accuracy) {
        this.requiredAccuracy = accuracy;
    }

    public void resetDialogState() {
        this.hasShownDialog = false;
    }

    public void evaluateAccuracy(int currentAccuracy) {
        boolean isInaccurate = false;

        // Check if current accuracy is below required accuracy
        if (currentAccuracy < requiredAccuracy) {
            isInaccurate = true;
        }

        if (isInaccurate) {
            if (!hasShownDialog) {
                showDialog(currentAccuracy);
                hasShownDialog = true; // only show dialog once per session
            } else if (dialog != null && dialog.isShowing()) {
                setAccuracyText(currentAccuracy);
            }
        } else {
            hideDialog();
        }
    }

    public void hideDialog() {
        if (dialog != null) {
            activity.runOnUiThread(() -> {
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                    accuracyTextView = null;
                }
            });
        }
    }

    private void showDialog(int currentAccuracy) {
        activity.runOnUiThread(() -> {
            Context context = activity;

            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            // Create a parent RelativeLayout for the dialog
            RelativeLayout dialogRelativeLayout = new RelativeLayout(context);
            dialogRelativeLayout.setId(RELATIVE_LAYOUT_ID);
            RelativeLayout.LayoutParams dialogRelativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            dialogRelativeLayout.setLayoutParams(dialogRelativeLayoutParams);
            dialogRelativeLayout.setBackgroundColor(0xFFFFFFFF);

            // Create title TextView and align it to the top of the parent layout
            TextView titleTextView = new TextView(context);
            titleTextView.setId(RELATIVE_LAYOUT_ID + 1);
            RelativeLayout.LayoutParams titleTextParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            titleTextParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            titleTextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            titleTextParams.setMargins(DEFAULT_HORIZONTAL_MARGIN, DEFAULT_HORIZONTAL_MARGIN, DEFAULT_HORIZONTAL_MARGIN, 0);
            titleTextView.setLayoutParams(titleTextParams);
            titleTextView.setText(DIALOG_TITLE);
            titleTextView.setTypeface(titleTextView.getTypeface(), Typeface.BOLD);
            titleTextView.setTextSize(22);
            titleTextView.setTextColor(0xFF000000);
            titleTextView.setGravity(android.view.Gravity.CENTER);
            dialogRelativeLayout.addView(titleTextView);

            // Create hint TextView and align it below the title TextView
            TextView hintTextView = new TextView(context);
            hintTextView.setId(RELATIVE_LAYOUT_ID + 2);
            RelativeLayout.LayoutParams hintTextParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            hintTextParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            hintTextParams.addRule(RelativeLayout.BELOW, titleTextView.getId());
            hintTextParams.setMargins(DEFAULT_HORIZONTAL_MARGIN, DEFAULT_VERTICAL_MARGIN, DEFAULT_HORIZONTAL_MARGIN, 0);
            hintTextView.setLayoutParams(hintTextParams);
            hintTextView.setText(CALIBRATION_HINT);
            hintTextView.setTextSize(18);
            hintTextView.setTextColor(0xFF000000);
            hintTextView.setGravity(android.view.Gravity.CENTER);
            dialogRelativeLayout.addView(hintTextView);

            // Create an ImageView and align it to the center of the parent layout
            ImageView imageView = new ImageView(context);
            imageView.setId(RELATIVE_LAYOUT_ID + 3);
            RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            imageParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            imageParams.setMargins(DEFAULT_HORIZONTAL_MARGIN, DEFAULT_VERTICAL_MARGIN, DEFAULT_HORIZONTAL_MARGIN, DEFAULT_VERTICAL_MARGIN);
            imageView.setLayoutParams(imageParams);

            // Try to load the calibration image resource
            int imageResID = context.getResources().getIdentifier(CALIBRATION_IMAGE_NAME, "drawable", context.getPackageName());
            if (imageResID != 0) {
                imageView.setImageResource(imageResID);
            }
            dialogRelativeLayout.addView(imageView);

            // Create a Button and align it to the bottom of the parent layout
            Button button = new Button(context);
            button.setId(RELATIVE_LAYOUT_ID + 4);
            RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            buttonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            buttonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            int buttonBottomOffset = 16;
            buttonParams.setMargins(DEFAULT_HORIZONTAL_MARGIN, DEFAULT_VERTICAL_MARGIN + buttonBottomOffset, DEFAULT_HORIZONTAL_MARGIN, DEFAULT_VERTICAL_MARGIN);

            button.setLayoutParams(buttonParams);
            button.setText("DONE");
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setTextColor(0xFF007AFF);
            button.setTranslationY(-(buttonBottomOffset) * context.getResources().getDisplayMetrics().density);
            dialogRelativeLayout.addView(button);

            // Create the accuracy TextView and align it above the button
            accuracyTextView = new TextView(context);
            accuracyTextView.setId(RELATIVE_LAYOUT_ID + 5);
            RelativeLayout.LayoutParams accuracyTextParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            accuracyTextParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            accuracyTextParams.addRule(RelativeLayout.ABOVE, button.getId());
            accuracyTextParams.setMargins(DEFAULT_HORIZONTAL_MARGIN, DEFAULT_VERTICAL_MARGIN, DEFAULT_HORIZONTAL_MARGIN, 0);
            accuracyTextView.setLayoutParams(accuracyTextParams);
            accuracyTextView.setTextSize(18);
            accuracyTextView.setTextColor(0xFF000000);
            accuracyTextView.setGravity(android.view.Gravity.CENTER);

            setAccuracyText(currentAccuracy);
            dialogRelativeLayout.addView(accuracyTextView);

            // Set the parent layout as the view of the builder
            builder.setView(dialogRelativeLayout);

            // Create and show the dialog
            dialog = builder.create();
            dialog.show();

            // Set a click listener for the button to dismiss the dialog
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideDialog();
                }
            });

            // Make the dialog modal by setting its cancelable properties to false
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        });
    }

    private void setAccuracyText(int accuracy) {
        if (accuracyTextView != null) {
            String accuracyName = getAccuracyName(accuracy);
            int accuracyColor = getAccuracyColor(accuracy);

            activity.runOnUiThread(() -> {
                if (accuracyTextView != null) {
                    // Clear the text view
                    accuracyTextView.setText("");

                    SpannableString compassAccuracyPrefix = new SpannableString("Compass accuracy: ");
                    compassAccuracyPrefix.setSpan(new ForegroundColorSpan(Color.BLACK), 0, compassAccuracyPrefix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    accuracyTextView.setText(compassAccuracyPrefix, TextView.BufferType.SPANNABLE);

                    SpannableString accuracyValue = new SpannableString(accuracyName);
                    accuracyValue.setSpan(new StyleSpan(Typeface.BOLD), 0, accuracyValue.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    accuracyValue.setSpan(new ForegroundColorSpan(accuracyColor), 0, accuracyValue.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    accuracyTextView.append(accuracyValue);
                }
            });
        }
    }

    private String getAccuracyName(int accuracy) {
        switch (accuracy) {
            case 3:
                return "HIGH";
            case 2:
                return "MEDIUM";
            case 1:
                return "LOW";
            case 0:
                return "UNRELIABLE";
            default:
                return "UNKNOWN";
        }
    }

    private int getAccuracyColor(int accuracy) {
        switch (accuracy) {
            case 3:
                return Color.GREEN;
            case 2:
                return Color.rgb(255, 165, 0); // orange
            case 1:
                return Color.RED;
            case 0:
                return Color.GRAY;
            default:
                return Color.BLACK;
        }
    }
}
