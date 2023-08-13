package trotter.max.memoryofagoldfish;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_DIALOG_RESPONSE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openCoinToss(View view) {
        Intent openCoinTossIntent = new Intent(getApplicationContext(), CoinTossActivity.class);
        openCoinTossIntent.putExtra("ExtraName", "ExtraValue");
        startActivityForResult(openCoinTossIntent, REQUEST_DIALOG_RESPONSE);
    }

    public void openUrl(View view) {
        Intent openUrlIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.goparker.com"));

        startActivity(openUrlIntent);
    }

    public void openList(View view) {
        Intent openListIntent = new Intent(getApplicationContext(), ListActivity.class);
        openListIntent.putExtra("ExtraName", "ExtraValue");
        startActivityForResult(openListIntent, REQUEST_DIALOG_RESPONSE);
    }

    public void openPuzzle1(View view) {
        Intent openListIntent = new Intent(getApplicationContext(), ListActivity.class);
        openListIntent.putExtra("Puzzle", "1");
        startActivityForResult(openListIntent, REQUEST_DIALOG_RESPONSE);
    }

    public void openPuzzle2(View view) {
        Intent openListIntent = new Intent(getApplicationContext(), ListActivity.class);
        openListIntent.putExtra("Puzzle", "2");
        startActivityForResult(openListIntent, REQUEST_DIALOG_RESPONSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_DIALOG_RESPONSE) {
            if(data.hasExtra("ResponseString")) {
                String responseString = data.getExtras().getString("ResponseString");
                Toast.makeText(getApplicationContext(),
                    "This is the response we got: " + responseString,
                    Toast.LENGTH_LONG).show();

            }
        }
    }
}