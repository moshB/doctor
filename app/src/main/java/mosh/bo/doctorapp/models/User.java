package mosh.bo.doctorapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class User implements Parcelable {
    String id;
    String email;
    Boolean isAvailable;
    Boolean isDoctor;
    String myDoctor;
    String next;

    public User(String id, String email, Boolean isAvailable, Boolean isDoctor, String previous, String next){
        this.id = id;
        this.email = email;
        this.isAvailable = isAvailable;
        this.isDoctor = isDoctor;
        this.myDoctor = previous;
        this.next = next;
    }
    public User(){}

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getMyDoctor() {
        return myDoctor;
    }

    public void setMyDoctor(String myDoctor) {
        this.myDoctor = myDoctor;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Boolean getAvailable() {
        return isAvailable;
    }
    public void setAvailable(Boolean available) {
        isAvailable = available;
    }
    public Boolean getDoctor() {
        return isDoctor;
    }
    public void setDoctor(Boolean doctor) {
        isDoctor = doctor;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", isAvailable=" + isAvailable +
                ", isDoctor=" + isDoctor +
                ", myDoctor='" + myDoctor + '\'' +
                ", next='" + next + '\'' +
                '}';
    }


    protected User(Parcel in) {
        isAvailable = in.readByte() != 0;
        isDoctor = in.readByte() != 0;
        id = in.readString();
        email = in.readString();
        myDoctor = in.readString();
        next = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isDoctor ? 1 : 0));
        dest.writeByte((byte) (isAvailable ? 1 : 0));
        dest.writeString(id);
        dest.writeString(email);
        dest.writeString(myDoctor);
        dest.writeString(next);
    }


    public boolean equals(User other) {
        if (this == other) return true;

        return other.getId().equals(id) &&
                other.getEmail().equals(email) &&
                other.getNext().equals(next) &&
                other.getMyDoctor().equals(myDoctor) &&
                other.getDoctor() == isDoctor &&
                other.getAvailable() == isAvailable ;
    }

}
