package com.home.treefrogapps.ChaChingExchange;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CustomAdapter extends ArrayAdapter<HashMap<String, String>> {

    int[] positionsToPin;
    String[] pinnedItems;

    boolean pinToggleOn;
    String currencyCode;

    SharedPreferences sharedPreferences;
    String pinnedPositionsToKeep;
    private final Context context;
    private final ArrayList<HashMap<String, String>> flagAndCurrencyList;

    private static class ViewHolder {
        protected ImageView flagType;
        protected TextView convertedCurrencyCode;
        protected TextView convertedCurrencyAmount;
        protected TextView convertedCurrencyType;
        protected TextView currencyRateText;
        protected ImageView pinToggle;
        protected ImageView offlineToggle;
        protected Button context_menu;

    }


    public CustomAdapter(Context context, ArrayList<HashMap<String, String>> flagAndCurrencyList) {
        super(context, R.layout.list_view_results, flagAndCurrencyList);

        // initialise preferences
        this.sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_MULTI_PROCESS);
        this.context = context;
        this.flagAndCurrencyList = flagAndCurrencyList;

    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        final HashMap<String, String> flagAndCurrencyItem = flagAndCurrencyList.get(position);

        final ViewHolder viewHolder;

        // Check if an existing view is being reused, otherwise inflate the view

        //viewHolder is used to reuse views that are cache rather than redrawing - offers better performance


        // do if the view has never been viewed/seen before
        if (convertView == null) {

            viewHolder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.list_view_results, parent, false);

            viewHolder.flagType = (ImageView) convertView.findViewById(R.id.flagType);
            viewHolder.convertedCurrencyAmount = (TextView) convertView.findViewById(R.id.convertedCurrencyAmount);
            viewHolder.convertedCurrencyCode = (TextView) convertView.findViewById(R.id.convertedCurrencyCode);
            viewHolder.convertedCurrencyType = (TextView) convertView.findViewById(R.id.convertedCurrencyType);
            viewHolder.currencyRateText = (TextView) convertView.findViewById(R.id.currencyRateText);
            viewHolder.pinToggle = (ImageView) convertView.findViewById(R.id.list_view_pin);
            viewHolder.offlineToggle = (ImageView) convertView.findViewById(R.id.list_view_offline);
            viewHolder.context_menu = (Button) convertView.findViewById(R.id.context_menu);

            // store the information in a tag
            convertView.setTag(viewHolder);

        } else {

            // if the view has been seen before use view lookup cache stored in tag
            viewHolder = (ViewHolder) convertView.getTag();
        }


        String name = flagAndCurrencyItem.get("flagType");
        int id = getContext().getResources().getIdentifier(name, "drawable", context.getPackageName());
        Drawable flagImage = getContext().getResources().getDrawable(id);

        // Populate the data into the template view using the data object
        viewHolder.flagType.setBackground(flagImage);
        viewHolder.convertedCurrencyAmount.setText(flagAndCurrencyItem.get("finalConvertedAmountText"));
        viewHolder.convertedCurrencyCode.setText(flagAndCurrencyItem.get("currencyCode"));
        viewHolder.convertedCurrencyType.setText(flagAndCurrencyItem.get("currencyType").substring(3));
        viewHolder.currencyRateText.setText(flagAndCurrencyItem.get("rateAmountText"));

        // get sharedPrefs using currency code as the key - if something exists
        // then there will be a string result for the current position currency
        // in the listView (test for string 'result' below)
        String result = sharedPreferences.getString(flagAndCurrencyItem.get("currencyCode"), "");
        // set the icon based on the result currency - all updating of listView handled in MainActivity
        if (!result.equals("")){
            viewHolder.offlineToggle.setBackground(getContext().getResources().getDrawable(R.drawable.ic_button_down_y));
        } else {
            viewHolder.offlineToggle.setBackground(getContext().getResources().getDrawable(R.drawable.ic_button_down_n));
        }

        // check the menuPinToggleState and put in relevant pin on / off
        if (pinToggleOn)
            viewHolder.pinToggle.setBackground(getContext().getResources().getDrawable(R.drawable.pin_button_on));
        else {
            viewHolder.pinToggle.setBackground(getContext().getResources().getDrawable(R.drawable.pin_button_off));
        }

        // context menu for each row (3 dots menu)
        // set setOnClickListener for button
        viewHolder.context_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View convertView) {

                //create popUpMenu (context menu)
                Context style = new ContextThemeWrapper(getContext(), R.style.PopUpMenu);

                PopupMenu popUpMenu = new PopupMenu(style, convertView);

                // inflate my context menu xml layout
                MenuInflater inflater = popUpMenu.getMenuInflater();
                inflater.inflate(R.menu.listview_popup_menu, popUpMenu.getMenu());

                // setOnMenuItemClickListener
                // perform task on each menu item click
                popUpMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_pin_currency:

                                if (pinToggleOn) {
                                    // this state happens only when the pinToggleButton is ON - do not want ability to 'pin' when this state is enabled,
                                    // will only enable when list contains ALL currencies but then it doesn't matter!

                                } else {

                                    // initialise positionsToPin in case the 'if' statement is invalid and skipped
                                    // otherwise nullPointerException
                                    positionsToPin = new int[flagAndCurrencyList.size()];

                                    // get shared prefs for pinned positions string (shared preferences were initialised onCreate all these key pairs come under "MyPrefs"
                                    pinnedPositionsToKeep = sharedPreferences.getString("PINNED_POSITIONS_TO_KEEP", "");

                                    if (pinnedPositionsToKeep.contains("[")) {

                                        Log.v("SAVED PREFERENCE STRING", pinnedPositionsToKeep);

                                        // function to change string which contains list of number in format [1,2,3,4] back to int array
                                        // remove [ ] from beginning of string

                                        pinnedItems = pinnedPositionsToKeep.substring(1, pinnedPositionsToKeep.length() - 1).split(",");
                                        positionsToPin = new int[pinnedItems.length];

                                        for (int i = 0; i < pinnedItems.length; i++) {

                                            positionsToPin[i] = Integer.parseInt(pinnedItems[i].trim());
                                        }

                                        for (int i = 0; i < positionsToPin.length; i++) {
                                            Log.v("SAVED PINNED POSITIONS", String.valueOf(positionsToPin[i]));
                                        }

                                    }
                                    // loop through entire list checking to pinned positions
                                    for (int i = 0; i < positionsToPin.length; i++) {

                                        if (position == i) {

                                            positionsToPin[i] = (position + 1);
                                        }
                                    }

                                    for (int i = 0; i < positionsToPin.length; i++) {
                                        Log.v("ADDED TO PIN LIST", String.valueOf(positionsToPin[i]));
                                    }

                                    // Cannot store int array in SharedPreferences - must be converted to String format
                                    String positionsToString = Arrays.toString(positionsToPin);

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    // Get SharedPreferences and store as a string - NO NEED TO CLEAR WHEN SWAPPING ACTIVITIES!
                                    // It is overwritten each time 'putString' 'apply' is used (not appended as its a concatenated single string!)
                                    editor.putString("PINNED_POSITIONS_TO_KEEP", positionsToString);
                                    editor.apply();

                                }
                                return true;

                            case R.id.menu_swap_to_base_currency:

                                // Toast.makeText(getContext(), "Set as base selected", Toast.LENGTH_SHORT).show();

                                currencyCode = viewHolder.convertedCurrencyCode.getText().toString();

                                // pass in the 3 letter currency code for the appropriate listItem
                                // DIRECTLY reference the method as it is static
                                // any reference inside the method to variables used elsewhere also have to be classed as static
                                // variables declared in the class don't
                                MainActivity.swapBaseCurrency(currencyCode);

                                return true;

                            case R.id.menu_remove_cache:

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                // get the current positions currency code from the ArrayList HashMap
                                String code = flagAndCurrencyItem.get("currencyCode");

                                // test if it present in sharedPrefs, if so remove it and update adapter
                                if (sharedPreferences.contains(code)){
                                    editor.remove(code);
                                    editor.apply();
                                    notifyDataSetChanged();

                                    String currentCurrencyCode = viewHolder.convertedCurrencyCode.getText().toString();

                                    if (MainActivity.currencyFromSpinner.getSelectedItem().toString().substring(0, 3).equals(currentCurrencyCode)){

                                        String baseSelector = "Cho";
                                        MainActivity.swapBaseCurrency(baseSelector);

                                    }

                                }
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popUpMenu.show();
            }
        });

        return convertView;
    }

}