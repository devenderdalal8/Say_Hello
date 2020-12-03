package com.devender.whatsapp.Fragment;

import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.devender.whatsapp.Fragment.ChatsFragment;
import com.devender.whatsapp.Fragment.ContactsFragment;
import com.devender.whatsapp.Fragment.GroupsFragment;

public class TabAccessorAdapter extends FragmentPagerAdapter {
    private TextView mText;
    public TabAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
                case 0:
                    return new ChatsFragment();
                case 1:
                    return new GroupsFragment();
                case 2:
                    return new ContactsFragment();
            case 3:
                return new RequestFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Contacts";
            case 3:
                return "Requests";
        }
        return null;
    }
}
