package in.testpress.store.ccavenue;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import in.testpress.store.R;


public class CityBankFragment extends Fragment implements View.OnClickListener{

    Button password;
    Button smsOtp;
    Communicator communicator;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.testpress_ccavenue_city_bank_layout,container,false);
        password = (Button) view.findViewById(R.id.password);
        smsOtp = (Button) view.findViewById(R.id.smsOtp);
        password.setOnClickListener(this);
        smsOtp.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        communicator = (Communicator) getActivity();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.password){
            communicator.respond("password");
            getActivity().getFragmentManager().beginTransaction().remove(this).commit();
        }
        else if(v.getId() == R.id.smsOtp){
            communicator.respond("smsOtp");
            getActivity().getFragmentManager().beginTransaction().remove(this).commit();
        }
    }
}
