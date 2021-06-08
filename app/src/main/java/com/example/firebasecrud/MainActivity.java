package com.example.firebasecrud;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String ARTIST_NAME = "artistname";
    public static final String ARTIST_ID = "artistid";

    EditText editTextName;
    Spinner spinnerGenre;
    Button btnAddArtist;

    ListView listViewArtists;

    List<Artist> artists;


    DatabaseReference databaseArtists;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        databaseArtists = FirebaseDatabase.getInstance().getReference("artists");


        editTextName = (EditText) findViewById(R.id.editTextName);
        btnAddArtist = (Button) findViewById(R.id.btnAddArtist);
        spinnerGenre = (Spinner) findViewById(R.id.spinnerGenre);
        listViewArtists = (ListView) findViewById(R.id.listViewArtists);

        artists = new ArrayList<>();
                btnAddArtist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                addArtist();
            }
        });
        listViewArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                Artist artist = artists.get(i);

                Intent intent = new Intent(getApplicationContext(), AddTrackActivity.class);

                intent.putExtra(ARTIST_ID, artist.getArtistId());
                intent.putExtra(ARTIST_NAME, artist.getArtistName());

                startActivity(intent);
            }
        });
        listViewArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                Artist artist = artists.get(i);
                showUpdateDeleteDialog(artist.getArtistId(), artist.getArtistName());
                return;

            }
        });
    }

    private void addArtist() {

        String name = editTextName.getText().toString().trim();
        String genre = spinnerGenre.getSelectedItem().toString();


        if (!TextUtils.isEmpty(name)) {


            String id = databaseArtists.push().getKey();


            Artist artist = new Artist(id, name, genre);


            databaseArtists.child(id).setValue(artist);


            editTextName.setText("");


            Toast.makeText(this, "Artist added", Toast.LENGTH_LONG).show();
        } else {

            Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onStart() {

        super.onStart();

        databaseArtists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                artists.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    Artist artist = postSnapshot.getValue(Artist.class);
                    artists.add(artist);
                }
                ArtistList artistAdapter = new ArtistList(MainActivity.this,artists);
                listViewArtists.setAdapter(artistAdapter);
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {

            }
        });
    }
    private void showUpdateDeleteDialog(String artistId, String artistName){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText)dialogView.findViewById(R.id.editTextName);
        final Spinner spinnerGenre = (Spinner)dialogView.findViewById(R.id.spinnerGenre);
        final Button buttonUpdate = (Button)dialogView.findViewById(R.id.buttonUpdateArtist);
        final Button buttonDelete = (Button)dialogView.findViewById(R.id.buttonDeleteArtist);

        dialogBuilder.setTitle("Updating Artist\n" + artistName);
        AlertDialog alertDialog =  dialogBuilder.create();
        alertDialog.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String genre = spinnerGenre.getSelectedItem().toString();

                if(TextUtils.isEmpty(name)){
                    editTextName.setError("Name is Required");
                    return;
                }
                updateArtist(artistId, name, genre);
                alertDialog.dismiss();
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteArtist(artistId);
                alertDialog.dismiss();
            }
            private void deleteArtist(String artistId){
                DatabaseReference dR = FirebaseDatabase.getInstance().getReference("artists").child(artistId);
                DatabaseReference drTracks = FirebaseDatabase.getInstance().getReference("tracks").child(artistId);

                dR.removeValue();
                drTracks.removeValue();

                Toast.makeText(getApplicationContext(), "Artist Deleted", Toast.LENGTH_LONG).show();

            }
        });
    }

    private boolean updateArtist(String id, String name, String genre) {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("artists").child(id);

        Artist artist = new Artist(id, name, genre);
        dR.setValue(artist);
        Toast.makeText(getApplicationContext(), "Artist Updated", Toast.LENGTH_LONG).show();
        return true;
    }
}
