package com.khan.chitchat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;

import javax.security.auth.Subject;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase,postRef,likeRef;
    private CircleImageView mProfileImage;
    private TextView mUserName;
    private LinearLayout mNavHeader;
    private RecyclerView recyclerView;
    private CardView cardView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter adapter;
    private Boolean likeChecker = false;
    private String current_user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //................................................
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        recyclerView = findViewById(R.id.recycler_view);
        cardView = findViewById(R.id.card_view);
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        current_user_id = mAuth.getCurrentUser().getUid();
        //................................................
        recyclerView.hasFixedSize();
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToAddPost();
                Toast.makeText(MainActivity.this, "Add New Post", Toast.LENGTH_SHORT).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        showProfileData();
        diaplayAllUsersPosts();

    }

    private void diaplayAllUsersPosts() {

        FirebaseRecyclerOptions<Posts> firebaseRecyclerOptions =
                new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(postRef,Posts.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Posts, PostViewHolder>(firebaseRecyclerOptions) {
            @Override
            public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_posts_list, parent, false);

                return new PostViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(PostViewHolder holder, final int position, Posts model) {
                // Bind the Chat object to the ChatHolder
                // ...
                final String PostKey = getRef(position).getKey();
                holder.setFullname(model.getFullname());
                holder.setDate(model.getDate());
                holder.setDescription(model.getDescription());
                holder.setProfileimage(getApplicationContext(),model.getProfileimage());
                holder.setPostimage(getApplicationContext(),model.getPostimage());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MainActivity.this,ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey",PostKey);
                        startActivity(clickPostIntent);
                    }
                });

                holder.setLikeButtonStatus(PostKey);
                holder.btnCommetns.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent postComment = new Intent(MainActivity.this,CommentsActivity.class);
                        postComment.putExtra("PostKey",PostKey);
                        startActivity(postComment);
                    }
                });
                holder.btnLikes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeChecker = true;

                        likeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (likeChecker.equals(true)){
                                    if (dataSnapshot.child(PostKey).hasChild(current_user_id)){
                                        likeRef.child(PostKey).child(current_user_id).removeValue();
                                        likeChecker = false;
                                    }
                                    else {
                                        likeRef.child(PostKey).child(current_user_id).setValue(true);
                                        likeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }

    private void goToAddPost() {
        Intent updatePost = new Intent(getApplicationContext(),AddPost.class);
        startActivity(updatePost);
    }
    private void showProfileData() {
        String current_user = mAuth.getCurrentUser().getUid();
        mDatabase.child(current_user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    mProfileImage = findViewById(R.id.profile_image_view);
                    mUserName = findViewById(R.id.txt_profile_name);
                    mNavHeader = findViewById(R.id.nav_header);
                    if (dataSnapshot.hasChild("profile image")){
                        String profileImage = dataSnapshot.child("profile image").getValue().toString();
                        Picasso.with(getApplicationContext()).load(profileImage).placeholder(R.drawable.profile_img)
                                .into(mProfileImage);
                    }
                    if (dataSnapshot.hasChild("fullname")){
                        String userName = dataSnapshot.child("fullname").getValue().toString();
                        mUserName.setText(userName);
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "No User Exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_logout){
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            goToLogin();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_add_new_post) {
            goToAddPost();
            Toast.makeText(this, "Add New Post", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_profile) {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_home) {

        } else if (id == R.id.nav_friends) {

        } else if (id == R.id.nav_find_friends) {

        } else if (id == R.id.nav_share) {

        }
        else if (id == R.id.nav_message) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView txtLikes;
        ImageButton btnLikes,btnCommetns;
        int likeCounts;
        String currentUserId;
        DatabaseReference likeRef;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            txtLikes = mView.findViewById(R.id.txt_likes);
            btnLikes = mView.findViewById(R.id.btn_like);
            btnCommetns = mView.findViewById(R.id.btn_comment);

            likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        public void setFullname(String fullname){
            TextView userName = mView.findViewById(R.id.posts_user_name);
            userName.setText(fullname);
        }
        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView image = mView.findViewById(R.id.posts_user_profile);
            Picasso.with(ctx).load(profileimage).into(image);
        }
        public void setPostimage(Context ctx, String postimage){
            ImageView post_image = mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(postimage).into(post_image);
        }
        public void setDescription(String description){
            TextView post_description = mView.findViewById(R.id.posts_description);
            post_description.setText(description);
        }
        public void setDate(String date){
            TextView post_date = mView.findViewById(R.id.post_date);
            post_date.setText("  "+date);
        }

        public void setLikeButtonStatus(final String postKey) {
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(currentUserId)){

                            likeCounts = (int) dataSnapshot.child(postKey).getChildrenCount();
                            btnLikes.setImageResource(R.drawable.like);
                            txtLikes.setText(String.valueOf(likeCounts+" likes"));
                    }
                    else {
                            likeCounts = (int) dataSnapshot.child(postKey).getChildrenCount();
                            btnLikes.setImageResource(R.drawable.dislike);
                            txtLikes.setText(String.valueOf(likeCounts+" likes"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
        {
            goToLogin();
        }
        else {
          checkUserData();
        }
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void checkUserData() {
        final String current_user = mAuth.getCurrentUser().getUid();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(current_user)){
                    goToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToSetupActivity() {
        Intent setupIntent = new Intent(getApplicationContext(),SetupActivity.class);
        startActivity(setupIntent);
        finish();
    }

    private void goToLogin() {
        Intent loginIntent = new Intent(getApplicationContext(),Login.class);
        startActivity(loginIntent);
        finish();
    }
}
