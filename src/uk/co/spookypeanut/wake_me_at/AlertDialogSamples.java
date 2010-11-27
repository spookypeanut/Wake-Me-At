/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.spookypeanut.wake_me_at;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;


/**
 * Example of how to use an {@link android.app.AlertDialog}.
 * <h3>AlertDialogSamples</h3>

<p>This demonstrates the different ways the AlertDialog can be used.</p>

<h4>Demo</h4>
App/Dialog/Alert Dialog
 
<h4>Source files</h4>
 * <table class="LinkTable">
 *         <tr>
 *             <td >src/com.example.android.apis/app/AlertDialogSamples.java</td>
 *             <td >The Alert Dialog Samples implementation</td>
 *         </tr>
 *         <tr>
 *             <td >/res/any/layout/alert_dialog.xml</td>
 *             <td >Defines contents of the screen</td>
 *         </tr>
 * </table> 
 */
public class AlertDialogSamples extends Activity {
    private static final int DIALOG_YES_NO_MESSAGE = 1;
    private static final int DIALOG_YES_NO_LONG_MESSAGE = 2;
    private static final int DIALOG_LIST = 3;
    private static final int DIALOG_PROGRESS = 4;
    private static final int DIALOG_SINGLE_CHOICE = 5;
    private static final int DIALOG_MULTIPLE_CHOICE = 6;
    private static final int DIALOG_TEXT_ENTRY = 7;
    private static final int DIALOG_MULTIPLE_CHOICE_CURSOR = 8;

    private static final int MAX_PROGRESS = 100;
    
    private ProgressDialog mProgressDialog;
    private int mProgress;
    private Handler mProgressHandler;

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
/*        case DIALOG_YES_NO_MESSAGE:
            return new AlertDialog.Builder(AlertDialogSamples.this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_two_buttons_title)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .create();
        case DIALOG_YES_NO_LONG_MESSAGE:
            return new AlertDialog.Builder(AlertDialogSamples.this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_two_buttons_msg)
                .setMessage(R.string.alert_dialog_two_buttons2_msg)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
    
                    }
                })
                .setNeutralButton(R.string.alert_dialog_something, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .create();
        case DIALOG_LIST:
            return new AlertDialog.Builder(AlertDialogSamples.this)
                .setTitle(R.string.select_dialog)
                .setItems(R.array.select_dialog_items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        String[] items = getResources().getStringArray(R.array.select_dialog_items);
                        new AlertDialog.Builder(AlertDialogSamples.this)
                                .setMessage("You selected: " + which + " , " + items[which])
                                .show();
                    }
                })
                .create();
        case DIALOG_PROGRESS:
            mProgressDialog = new ProgressDialog(AlertDialogSamples.this);
            mProgressDialog.setIcon(R.drawable.alert_dialog_icon);
            mProgressDialog.setTitle(R.string.select_dialog);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMax(MAX_PROGRESS);
            mProgressDialog.setButton(getText(R.string.alert_dialog_hide), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });
            mProgressDialog.setButton2(getText(R.string.alert_dialog_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });
            return mProgressDialog;
        case DIALOG_SINGLE_CHOICE:
            return new AlertDialog.Builder(AlertDialogSamples.this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_single_choice)
                .setSingleChoiceItems(R.array.select_dialog_items2, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
               .create();
        case DIALOG_MULTIPLE_CHOICE:
            return new AlertDialog.Builder(AlertDialogSamples.this)
                .setIcon(R.drawable.ic_popup_reminder)
                .setTitle(R.string.alert_dialog_multi_choice)
                .setMultiChoiceItems(R.array.select_dialog_items3,
                        new boolean[]{false, true, false, true, false, false, false},
                        new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton,
                                    boolean isChecked) {

                            }
                        })
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
               .create();
            case DIALOG_MULTIPLE_CHOICE_CURSOR:
                String[] projection = new String[] {
                        Contacts.People._ID,
                        Contacts.People.NAME,
                        Contacts.People.SEND_TO_VOICEMAIL
                };
                Cursor cursor = managedQuery(Contacts.People.CONTENT_URI, projection, null, null, null);
                return new AlertDialog.Builder(AlertDialogSamples.this)
                    .setIcon(R.drawable.ic_popup_reminder)
                    .setTitle(R.string.alert_dialog_multi_choice_cursor)
                    .setMultiChoiceItems(cursor,
                            Contacts.People.SEND_TO_VOICEMAIL,
                            Contacts.People.NAME,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton,
                                        boolean isChecked) {
                                    Toast.makeText(AlertDialogSamples.this,
                                            "Readonly Demo Only - Data will not be updated",
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                   .create();
                   */
        case DIALOG_TEXT_ENTRY:
            // This example shows how to add a custom layout to an AlertDialog
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.text_input, null);
            return new AlertDialog.Builder(AlertDialogSamples.this)
                .setIcon(R.drawable.x)
                .setTitle("blah")
                .setView(textEntryView)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
    
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .create();
        }
        return null;
    }


protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    showDialog(DIALOG_TEXT_ENTRY);
}
}