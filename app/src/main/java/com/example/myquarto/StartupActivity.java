package com.example.myquarto;

/**
 * In questa classe vengono definiti tutte le entità all'interno della prima interfaccia utente
 * Vengono raccolti tutti i dati e le modalità di gioco
 *
 * OnCreate è il "builder" dell'interfaccia
 * Metodi:
 * setupSwitch(SwitchCompat aSwitch, boolean initiallyChecked)
 * collectSettingsAndStartGame()
 */

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class StartupActivity extends AppCompatActivity {

    public static final String EXTRA_TIMER_ENABLED = "com.example.quarto_android.TIMER_ENABLED";
    public static final String EXTRA_TIMER_MINUTES = "com.example.quarto_android.TIMER_MINUTES";
    public static final String EXTRA_WIN_PREFERENCES = "com.example.quarto_android.WIN_PREFERENCES";

    private SwitchCompat switchEnableTimer;
    private EditText editTextTimerMinutes;
    private LinearLayout layoutTimerMinutes;
    private SwitchCompat switchWinRows;
    private SwitchCompat switchWinCols;
    private SwitchCompat switchWinDiag;
    private SwitchCompat switchWin2x2;
    private SwitchCompat switchWin3x3;
    private SwitchCompat switchWin4x4;
    private Button buttonStartGame; // anche se la variabile viene usata solo in OnCreate è inizializzata come tutti gli altri elementi dell'interfaccia

    private final float ALPHA_ENABLED = 1.0f;
    private final float ALPHA_DISABLED = 0.4f; // Trasparenza elementi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_window);

        // Inizializza le view
        switchEnableTimer = findViewById(R.id.switch_enable_timer);
        editTextTimerMinutes = findViewById(R.id.editText_timer_minutes);
        layoutTimerMinutes = findViewById(R.id.layout_timer_minutes);

        switchWinRows = findViewById(R.id.switch_win_rows);
        switchWinCols = findViewById(R.id.switch_win_cols);
        switchWinDiag = findViewById(R.id.switch_win_diag);
        switchWin2x2 = findViewById(R.id.switch_win_2x2);
        switchWin3x3 = findViewById(R.id.switch_win_3x3);
        switchWin4x4 = findViewById(R.id.switch_win_4x4);
        buttonStartGame = findViewById(R.id.button_start_game);

        // Impostazioni listeners (Timer disabilitato, vittorie tutte attive tranne la 3x3)
        setupSwitch(switchEnableTimer, false);
        setupSwitch(switchWinRows, true);
        setupSwitch(switchWinCols, true);
        setupSwitch(switchWinDiag, true);
        setupSwitch(switchWin2x2, true);
        setupSwitch(switchWin3x3, false);
        setupSwitch(switchWin4x4, true);

        // Logica per switchEnableTimer per controllare editText e la sua label
        switchEnableTimer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonView.setAlpha(isChecked ? ALPHA_ENABLED : ALPHA_DISABLED);
            // Controlla l'alpha e l'enabled state del layout per i minuti del timer
            layoutTimerMinutes.setAlpha(isChecked ? ALPHA_ENABLED : ALPHA_DISABLED);
            editTextTimerMinutes.setEnabled(isChecked);
        });
        // Impostazioni stato iniziale per il layout dei minuti
        boolean timerInitiallyEnabled = switchEnableTimer.isChecked();
        layoutTimerMinutes.setAlpha(timerInitiallyEnabled ? ALPHA_ENABLED : ALPHA_DISABLED);
        editTextTimerMinutes.setEnabled(timerInitiallyEnabled);

        buttonStartGame.setOnClickListener(v -> collectSettingsAndStartGame());
    }

    private void setupSwitch(SwitchCompat aSwitch, boolean initiallyChecked) {
        aSwitch.setChecked(initiallyChecked);
        aSwitch.setAlpha(initiallyChecked ? ALPHA_ENABLED : ALPHA_DISABLED);
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonView.setAlpha(isChecked ? ALPHA_ENABLED : ALPHA_DISABLED);
        });
    }

    // Prende tutte le impostazioni
    private void collectSettingsAndStartGame() {
        boolean timerEnabled = switchEnableTimer.isChecked();
        int timerMinutes = Integer.parseInt(getString(R.string.default_timer_minutes));

        if (timerEnabled) {
            try {
                String minutesText = editTextTimerMinutes.getText().toString();
                if (!minutesText.isEmpty()) {
                    timerMinutes = Integer.parseInt(minutesText);
                }
                if (timerMinutes <= 0) {
                    timerMinutes = Integer.parseInt(getString(R.string.default_timer_minutes));
                    Toast.makeText(this, String.format(getString(R.string.toast_invalid_time_default), timerMinutes), Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, String.format(getString(R.string.toast_invalid_time_default), timerMinutes), Toast.LENGTH_SHORT).show();
            }
        }

        boolean[] winPreferences = new boolean[]{
                switchWinRows.isChecked(),
                switchWinCols.isChecked(),
                switchWinDiag.isChecked(),
                switchWin2x2.isChecked(),
                switchWin3x3.isChecked(),
                switchWin4x4.isChecked()
        };

        // consegna dei settings al main
        Intent intent = new Intent(StartupActivity.this, MainActivity.class);
        intent.putExtra(EXTRA_TIMER_ENABLED, timerEnabled);
        intent.putExtra(EXTRA_TIMER_MINUTES, timerMinutes);
        intent.putExtra(EXTRA_WIN_PREFERENCES, winPreferences);

        startActivity(intent);
        finish();
    }
}