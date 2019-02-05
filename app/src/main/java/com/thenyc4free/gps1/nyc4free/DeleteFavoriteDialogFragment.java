package com.thenyc4free.gps1.nyc4free;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Class to create Alert dialog when the user clicks on the delete button in the "favorites" view,
 * to confirm or cancel user deletion of favorite events and to instantiate the listener to
 * communicate user's selection to the host so the favorite event can be properly deleted or not.
 */

public class DeleteFavoriteDialogFragment extends DialogFragment {

    public interface DeleteFavoriteDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    private DeleteFavoriteDialogListener mListener;

    public DeleteFavoriteDialogFragment() {}

    // Override the Fragment.onAttach() method to instantiate the DeleteFavoriteDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DeleteFavoriteDialogListener so we can send events to the host
            mListener = (DeleteFavoriteDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(String.format(getContext()
                            .getString(R.string.delete_favorite_listener_error_msg),
                    getActivity().toString()));
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(R.string.favorite_will_be_deleted)
                .setNegativeButton(R.string.cancel_delete_favorite, new DialogInterface
                        .OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Send the negative button event back to the host activity
                            mListener.onDialogNegativeClick(DeleteFavoriteDialogFragment.this);
                        }
                })
                .setPositiveButton(R.string.delete_favorite, new DialogInterface
                        .OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Send the positive button event back to the host activity
                            mListener.onDialogPositiveClick(DeleteFavoriteDialogFragment.this);
                        }
                });
            // Create the AlertDialog object and return it
            return builder.create();
    }
}
