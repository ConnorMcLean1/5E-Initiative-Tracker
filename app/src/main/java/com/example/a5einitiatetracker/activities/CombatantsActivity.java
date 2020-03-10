package com.example.a5einitiatetracker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.example.a5einitiatetracker.R;
import com.example.a5einitiatetracker.api.APIUtility;
import com.example.a5einitiatetracker.api.json.JSONUtility;
import com.example.a5einitiatetracker.combatant.Combatant;
import com.example.a5einitiatetracker.combatant.Monster;
import com.example.a5einitiatetracker.combatant.NPC;
import com.example.a5einitiatetracker.combatant.Player;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CombatantsActivity extends AppCompatActivity {

    private LinearLayout parentLinearLayout;
    HashMap<String, String> monsterNames;
    public static List<Combatant> combatantsList = new ArrayList<>();
    private static boolean validCombatants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combatants);

        parentLinearLayout = findViewById(R.id.lnrLayoutMonsters);
        //Reads in the list of monster Names and Indexes from the JSON file created on startup
        monsterNames = JSONUtility.readMonsternamesFromJSONFile(this.getApplicationContext(), JSONUtility.JSON_FILE_NAME);

        String[] monsters = new String[monsterNames.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : monsterNames.entrySet()) {
            monsters[i] = entry.getKey();
            Log.d("myTAG", monsters[i]);
            i++;
        }


        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, monsters);

        FloatingActionButton addMonsterButton = findViewById(R.id.fabAddMonster);
        addMonsterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View rowView = inflater.inflate(R.layout.monster_entry_layout, null);
                final AutoCompleteTextView autoCompleteTextView = rowView.findViewById(R.id.autoTxtViewMonsters);
                final EditText numberEditText = rowView.findViewById(R.id.editTxtMonsterNumber);
                autoCompleteTextView.setAdapter(arrayAdapter);
                parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount());
            }
        });

        FloatingActionButton addPlayerButton = findViewById(R.id.fabAddPlayer);
        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View rowView = inflater.inflate(R.layout.player_entry_layout, null);
                parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        combatantsList.clear();
    }

    public void startCombat() {
        Intent intent = new Intent(getBaseContext(), CombatActivity.class);
        startActivity(intent);
    }

    // loads all the monster info and adds them to the combatant list, then sorts the list by
    // initiative
    public void getMonsterData(View v) {
        //If the combatants list is already created then clear it before adding new elements to it
        if(!combatantsList.isEmpty())
            combatantsList.clear();
        //If the user tries to start an empty combat, prevent it and display a message
        if(parentLinearLayout.getChildCount() == 0){
            Toast.makeText(this.getApplicationContext(), "Please add at least one combatant to the combat to start it!", Toast.LENGTH_SHORT).show();
            return;
        }

        int combatantCount = parentLinearLayout.getChildCount();
        View view;
        AutoCompleteTextView name;
        EditText num;
        int number;
        for (int i = 0; i < combatantCount; i++) {
            view = parentLinearLayout.getChildAt(i);
            if (!view.getTag().toString().equals("player_entry")) {
                num = view.findViewById(R.id.editTxtMonsterNumber);
                name = view.findViewById(R.id.autoTxtViewMonsters);
               try{
                   number = Integer.parseInt(num.getText().toString());
                   validCombatants = true;
               }
               catch (NumberFormatException e) {
                   num.setBackgroundColor(Color.parseColor("#f54242"));
                   validCombatants = false;
               }

                String monster = name.getText().toString(), temp;
                ListAdapter adapter = name.getAdapter();
                int j;

                for(j = 0; j < adapter.getCount(); j++){
                    temp = adapter.getItem(j).toString();
                    if(monster.compareTo(temp) == 0){
                        name.setBackgroundColor(Color.parseColor("#ffffff"));
                        validCombatants = true;
                        break;
                    }
                    if(j == adapter.getCount()){
                        name.setBackgroundColor(Color.parseColor("#f54242"));
                        validCombatants = false;
                    }
                }
            }
        }



        if(validCombatants) {
            getPlayers();
            final HashMap<String, Integer> monsters = createMonsterKeyValuePair();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String monsterIndex;
                    String monsterName;
                    for (HashMap.Entry<String, Integer> entry : monsters.entrySet()) {
                        monsterIndex = monsterNames.get(entry.getKey());
                        for (int i = 0; i < entry.getValue(); i++) {
                            NPC m = APIUtility.getMonsterByIndex(monsterIndex);
                            monsterName = String.format("%s %d", m.getName(), i + 1);
                            m.setName(monsterName);
                            combatantsList.add(m);
                        }
                    }

                    Collections.sort(combatantsList, Collections.<Combatant>reverseOrder());
                    Log.v("LIST", combatantsList.toString());
                    startCombat();
                }
            }).start();
        }
        else {
            Toast.makeText(this.getApplicationContext(), "One of the monsters is not valid. Please ensure all fields are valid before continuing.", Toast.LENGTH_SHORT).show();
        }
    }

    // loads all the players into the combatant list
    private void getPlayers() {
        int combatantCount = parentLinearLayout.getChildCount();
        View v;
        EditText playerNameEditText, playerInitiativeEditText;
        String name;
        int initiative;
        for (int i = 0; i < combatantCount; i++) {
            v = parentLinearLayout.getChildAt(i);
            if (v.getTag().toString().equals("player_entry")) {
                playerNameEditText = v.findViewById(R.id.editTxtPlayerName);
                playerInitiativeEditText = v.findViewById(R.id.editTxtInitiative);
                name = playerNameEditText.getText().toString();
                initiative = Integer.parseInt(playerInitiativeEditText.getText().toString());
                Player p = new Player(initiative, name);
                combatantsList.add(p);
            }
        }
    }

    // creates a hashmap of all the monsters with their name and the number to load
    private HashMap createMonsterKeyValuePair() {
        HashMap<String, Integer> m = new HashMap<String, Integer>();
        int combatantCount = parentLinearLayout.getChildCount();
        View view;
        AutoCompleteTextView name;
        EditText num;
        for (int i = 0; i < combatantCount; i++) {
            view = parentLinearLayout.getChildAt(i);
            if (!view.getTag().toString().equals("player_entry")) {
                num = view.findViewById(R.id.editTxtMonsterNumber);
                name = view.findViewById(R.id.autoTxtViewMonsters);
                m.put(
                        name.getText().toString(),
                        Integer.parseInt(num.getText().toString())
                );
            }
        }
        // for debugging
        for (Map.Entry<String, Integer> entry : m.entrySet()) {
            Log.v("MAP", String.format("Key: %s - Value: %s", entry.getKey(), entry.getValue()));
        }

        return m;
    }

    public void onDelete(View v) {
        parentLinearLayout.removeView((View) v.getParent());
    }

}
