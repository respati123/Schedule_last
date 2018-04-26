package com.android.victory.schedule.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.text.InputType;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.victory.schedule.R;
import com.android.victory.schedule.data.Model;
import com.android.victory.schedule.data.Schedule;
import com.android.victory.schedule.data.StateSaveActivity;
import com.android.victory.schedule.service.NetworkService;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView scheduleImageView, monitorImageView, syncStateImageView;
    private TextView timeTextView;
    public NavigationView navigationView;

    public TextView logoNameTextView;
    public ImageView logoPhotoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        scheduleImageView = (ImageView)findViewById(R.id.schedule_imageview);
        monitorImageView = (ImageView)findViewById(R.id.monitor_imageivew);
        syncStateImageView = (ImageView)findViewById(R.id.sync_state_imageview);
        timeTextView = (TextView)findViewById(R.id.current_time_textview);

        if (Model.getInstance().isVailable)syncStateImageView.setBackground(getResources().getDrawable(R.drawable.online_background));
        else syncStateImageView.setBackground(getResources().getDrawable(R.drawable.offline_background));

        scheduleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ScheduleActivity.class));
                finish();
            }
        });

        monitorImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
                finish();
            }
        });

        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSyncMenu(v);
            }
        });

        timeTextView.setText(DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        logoNameTextView = (TextView)headerView.findViewById(R.id.logoname_textview);
        logoPhotoView = (CircleImageView)headerView.findViewById(R.id.photo_imageview);

        if (StateSaveActivity.readPhoto(MainActivity.this) != null)logoPhotoView.setImageBitmap(StateSaveActivity.readPhoto(MainActivity.this));
        logoPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
            }
        });

        logoNameTextView.setText(Model.getInstance().username);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showSyncMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.sync_item:
                        Toast.makeText(MainActivity.this, "Synchronize", Toast.LENGTH_SHORT).show();
                        startService(new Intent(MainActivity.this,NetworkService.class ));
                        return true;
                    case R.id.cancel_item:
                        Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                        return false;
                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.sync_menu);
        popup.show();
    }

    public void showAccountMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.photo_item:
                        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
                        return true;
                    case R.id.pwd_item:
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setTitle("\tPASSWORD\n");
                        alertDialog.setMessage("Enter Password");

                        final EditText input = new EditText(MainActivity.this);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        input.setLayoutParams(lp);
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        alertDialog.setView(input);
                        alertDialog.setIcon(R.drawable.key);

                        alertDialog.setPositiveButton("YES",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (input.getText().toString().isEmpty())input.setError("Password is needed");
                                        else {
                                            Model.getInstance().password = input.getText().toString();
                                            Toast.makeText(MainActivity.this, "Create", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                        alertDialog.setNegativeButton("NO",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                                        dialog.cancel();
                                    }
                                });

                        alertDialog.show();
                        return false;
                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.account_menu);
        popup.show();
    }

    public void showSettingMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.lan_item:
                        return false;
                    case R.id.eng_item:
                        Model.getInstance().isEnglish = true;
                        return false;
                    case R.id.ind_item:
                        Model.getInstance().isEnglish = false;
                        return false;
                    case R.id.age_item:
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setTitle("\tAge of local data\n\n\n\n\n");
                        alertDialog.setMessage("Enter Age(month)");

                        final EditText input = new EditText(MainActivity.this);

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        input.setLayoutParams(lp);
                        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                        alertDialog.setView(input);

                        alertDialog.setPositiveButton("YES",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (input.getText().toString().isEmpty())input.setError("Age is needed");
                                        else {
                                            Model.getInstance().localPeriodTime = Integer.parseInt(input.getText().toString()) * 30 * 24 * 60 * 1000;
                                            startService(new Intent(MainActivity.this, NetworkService.class));
                                            Toast.makeText(MainActivity.this, "Create", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                        alertDialog.setNegativeButton("NO",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                                        dialog.cancel();
                                    }
                                });

                        alertDialog.show();
                        return false;
                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.setting_menu);
        popup.show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_setting) {
            // Handle the camera action
            showSettingMenu(navigationView);
        } else if (id == R.id.nav_account) {
            showAccountMenu(navigationView);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    StateSaveActivity.writePhoto(MainActivity.this, bitmap);
                    logoPhotoView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}



