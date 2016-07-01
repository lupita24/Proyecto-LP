package com.example.joselhm.safepath_droid;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.Profile;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

public class NavigatorMapas extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Constantes configuration;

    private LayoutInflater inflater;
    private RelativeLayout contenedor;

    private TextView nickname;
    private TextView email;
    private ImageView imgProfile;
    private View header;
    private NavigationView navigationView;

    //Google Analitics
    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "UA-79249503-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigator_mapas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Vamos a la funcion Añadir Zona - addZona
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addZona();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //My functions
        IniConfiguration();
        IniComponents();
        setMyProfile();
        funMisRutas();

        //Google Analytics
        Ini_GoogleAnalitics();
    }

    public void Ini_GoogleAnalitics()
    {
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker(PROPERTY_ID);
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigator_mapas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.search:
                metodoSearch_rutas();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_cuenta) {
            funMiCuenta();
        } else if (id == R.id.nav_rutas) {
            funMisRutas();
        } else if (id == R.id.nav_calificaciones) {

        } else if (id == R.id.nav_configuracion) {

        } else if (id == R.id.nav_about) {
            funcAbout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //My FUNCTIONS....
    public void IniConfiguration(){
        DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();
        int w = displayMetrics.widthPixels;
        int h = displayMetrics.heightPixels;
        configuration = new Constantes(w,h, this);
    }
    //Inicializamos los componentes
    public void IniComponents()
    {
        header = navigationView.getHeaderView(0);
        nickname = (TextView) header.findViewById(R.id.nicknameView);
        email = (TextView) header.findViewById(R.id.text_email);
        imgProfile = (ImageView) header.findViewById(R.id.imageView);

        contenedor = (RelativeLayout)findViewById(R.id.contend_layout);
        inflater = LayoutInflater.from(this);
    }

    //Seteamos elnickname e imgProfile por uno por defecto, lo que nosotros queramos
    public void myDefaultProlife()
    {
        SharedPreferences miCuenta = getSharedPreferences(Constantes.MY_PREFS_NAME,this.MODE_PRIVATE);
        String name = miCuenta.getString("nombre", "no found");
        String email_ = miCuenta.getString("clave","no found");
        nickname.setText(name);
        email.setText(email_);
        Drawable imgProfile_ = new BitmapDrawable(this.getResources(),configuration.escalarImagen("icons/userProfile.png",100,100));
        imgProfile.setBackground(imgProfile_);
    }

    public void setMyProfile()
    {
        try{
            if (Profile.getCurrentProfile() != null) {
                nickname.setText(Profile.getCurrentProfile().getName());
                imgProfile = (ImageView) header.findViewById(R.id.imageView);
                Picasso.with(getApplicationContext())
                        .load("https://graph.facebook.com/" + Profile.getCurrentProfile().getId()+ "/picture?type=small")
                        .into(imgProfile);
            }else{
                myDefaultProlife();

            }
        }catch (Exception e)
        {
            //error al cargar miprofile
        }
    }

    //Inicializamos mapa como primera vista
    public void funMisRutas()
    {
        try {
            //Infamos otra vez el content_navigator_mapas que contiene un fragment
            contenedor.removeAllViews();
            inflater.inflate(R.layout.content_navigator_mapas, contenedor, true);
            //Reemplazamos el Fragmente por el de MapaGeneral
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.contend_frame, new MapaGeneral()).commit();
            hide_Btn_AddCalific(false);
        }catch (Exception e){
            //Error al pasar el activity MapaGeneral
        }
    }

    //Se abrira otro activity para añadir zona
    public void addZona()
    {
        try{
            //Infamos otra vez el content_navigator_mapas que contiene un fragment
            contenedor.removeAllViews();
            inflater.inflate(R.layout.content_navigator_mapas, contenedor, true);
            //Reemplazamos el Fragmente por el de addZona
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.contend_frame,new AddZona()).commit();
            hide_Btn_AddCalific(true);
        }catch (Exception e){
            //Error al pasar el activity AddZona
        }
    }
    //Ocultamos el boton
    public void hide_Btn_AddCalific(boolean hide)
    {
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        if(hide){
            fab.setVisibility(View.INVISIBLE);
        }else{
            fab.setVisibility(View.VISIBLE);
        }
    }

    //Funcion About Safe Path
    public void funcAbout()
    {
        try{
            contenedor.removeAllViews();
            inflater.inflate(R.layout.activity_about, contenedor, true);
            hide_Btn_AddCalific(true);
        }catch (Exception e){
            //Error al pasar al actvity Registrarse
        }
    }

    //Funcion Mi cuenta
    public  void funMiCuenta()
    {
        try{
            contenedor.removeAllViews();
            inflater.inflate(R.layout.activity_mi_cuenta, contenedor, true);
            hide_Btn_AddCalific(true);
        }catch (Exception e){
            //Error al pasar al actvity Registrarse
        }
    }

    //
    public void metodoSearch_rutas()
    {
        try{
            Intent intent = new Intent(this,MapaRutas.class);
            startActivity(intent);
            overridePendingTransition(R.anim.my_fade_in, R.anim.my_fade_out);
            //finish(); //Sirve para cerrar definitivamente el activity actual al pasar a otro
        }catch (Exception e){
            //Error al pasar al actvity Registrarse
        }
    }
}
