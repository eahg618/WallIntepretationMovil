package com.zircon.mx.wallintepretationmovil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultRegistryOwner;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;


public class DeviceListFragment extends Fragment implements AbsListView.OnItemClickListener {

    private ArrayList<com.zircon.mx.wallintepretationmovil.DeviceItem> deviceItemList;

    private OnFragmentInteractionListener mListener;
    private static BluetoothAdapter bTAdapter;
    public boolean accesoConcedido = false;

    private AbsListView mListView;

    private ArrayAdapter<com.zircon.mx.wallintepretationmovil.DeviceItem> mAdapter;
    private List<String> DevicesList = new ArrayList<>();
    private String deviceAddress = "";
    private final BroadcastReceiver bReciever = new BroadcastReceiver() {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("DEVICELIST", "Bluetooth device found\n");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                @SuppressLint("MissingPermission") com.zircon.mx.wallintepretationmovil.DeviceItem newDevice = new com.zircon.mx.wallintepretationmovil.DeviceItem(device.getName(), device.getAddress(), "false");

                if (DevicesList.size() == 0) {
                    deviceAddress = newDevice.getAddress();
                    DevicesList.add(deviceAddress);
                    mAdapter.add(newDevice);
                    mAdapter.notifyDataSetChanged();

                } else {
                    String address = newDevice.getAddress();
                    String name = newDevice.getDeviceName();

                    if (address != null && address != "" && name != null && name != "") {
                        Predicate<String> Exist = new Predicate<String>() {
                            @Override
                            public boolean test(String s) {
                                boolean a = false;
                                if (s.equals(address))
                                    a = true;
                                else
                                    a = false;
                                return a;
                            }
                        };
                        boolean a = DevicesList.stream().anyMatch(Exist);
                        if (a == false) {
                            DevicesList.add(address);
                            mAdapter.add(newDevice);
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                }


            }
        }
    };


    public static DeviceListFragment newInstance(BluetoothAdapter adapter) {
        DeviceListFragment fragment = new DeviceListFragment();
        bTAdapter = adapter;
        return fragment;
    }

    public DeviceListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("DEVICELIST", "Super called for DeviceListFragment onCreate\n");
        deviceItemList = new ArrayList<com.zircon.mx.wallintepretationmovil.DeviceItem>();
        @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = bTAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                @SuppressLint("MissingPermission") com.zircon.mx.wallintepretationmovil.DeviceItem newDevice = new com.zircon.mx.wallintepretationmovil.DeviceItem(device.getName(), device.getAddress(), "false");
                deviceItemList.add(newDevice);
            }
        }

        // If there are no devices, add an item that states so. It will be handled in the view.
        if (deviceItemList.size() == 0) {
            deviceItemList.add(new com.zircon.mx.wallintepretationmovil.DeviceItem("No Devices", "", "false"));
        }

        Log.d("DEVICELIST", "DeviceList populated\n");
        mAdapter = new com.zircon.mx.wallintepretationmovil.DeviceListAdapter(getActivity(), deviceItemList, bTAdapter);
        Log.d("DEVICELIST", "Adapter created\n");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deviceitem_list, container, false);
        ToggleButton scan = (ToggleButton) view.findViewById(R.id.scan);
        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        scan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("MissingPermission")
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
                if (isChecked) {
                    mAdapter.clear();
                    getActivity().registerReceiver(bReciever, filter);
                    if (bTAdapter.isDiscovering()) {
                        bTAdapter.cancelDiscovery();
                    }
                    if (accesoConcedido)
                        bTAdapter.startDiscovery();
                } else {
                    getActivity().unregisterReceiver(bReciever);
                    bTAdapter.cancelDiscovery();
                    DevicesList.clear();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Log.d("DEVICELIST", "onItemClick position: " + position + " id: " + id + " name: " + deviceItemList.get(position).getDeviceName() + "\n");
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(deviceItemList.get(position).getDeviceName());
        }
        Bundle extras = new Bundle();

        extras.putString("Address", deviceItemList.get(position).getAddress()); // se obtiene el valor mediante getString(...)
        extras.putString("Device Name", deviceItemList.get(position).getDeviceName()); // se obtiene el valor mediante getInt(...)

        Intent intent = new Intent(this.getActivity(), MainActivity.class);
        //Agrega el objeto bundle a el Intne
        intent.putExtras(extras);
        //Inicia Activity
        startActivity(intent);
    }


    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
