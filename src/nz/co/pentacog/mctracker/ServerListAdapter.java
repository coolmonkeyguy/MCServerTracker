/**
 * 
 */
package nz.co.pentacog.mctracker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import nz.co.pentacog.mctracker.GetServerDataTask.ServerDataResultHandler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author Affian
 *
 */
public class ServerListAdapter extends BaseAdapter implements Filterable {
	
	private ArrayList<Server> serverList = null;

	public ServerListAdapter() {
		this(new ArrayList<Server>());
	}
	
	/**
	 * 
	 */
	public ServerListAdapter(ArrayList<Server> serverList) {
		this.serverList = serverList;
		
		try {
			serverList.add(new Server("My Server", InetAddress.getByName("192.168.2.118")));
			serverList.add(new Server("Blake's Server", InetAddress.getByName("182.160.139.146")));
			serverList.add(new Server("1.7 Server via URL", InetAddress.getByName("server.aussiegamerhub.com")));
			serverList.add(new Server("Localhost - No Server", InetAddress.getByName("localhost")));
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return serverList.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Server getItem(int position) {
		return serverList.get(position);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemViewType(int)
	 */
	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		RelativeLayout serverView = null;
		ServerViewHolder holder = null;
		Server server = serverList.get(position);
		
		if (convertView == null) {
			serverView = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
			holder = new ServerViewHolder(serverView);
			serverView.setTag(holder);
		} else {
			serverView = (RelativeLayout) convertView;
			holder = (ServerViewHolder) serverView.getTag();
		}
		
		
		//set server name
		holder.serverTitle.setText(server.name);
		//set server IP
		String serverName = server.address.toString();
		if (!serverName.startsWith("/")) {
			int index = serverName.lastIndexOf('/');
			String tempString;
			tempString = serverName.substring(index+1);
			serverName = serverName.substring(0, index);
			serverName += " " + tempString;
		} else {
			serverName = serverName.replace("/", "");
		}
		holder.serverIp.setText(serverName + ":" + server.port);
		
		if (!server.queried) {
			holder.loading.setVisibility(View.VISIBLE);
			holder.playerCount.setText("" + server.playerCount + "/" + server.maxPlayers);
			holder.serverData.setText(R.string.loading);
			new ServerViewUpdater(serverView, server);
		} else {
			holder.loading.setVisibility(View.GONE);
			holder.playerCount.setText("" + server.playerCount + "/" + server.maxPlayers);
			holder.serverData.setText(server.motd);
		}
		
		return serverView;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getViewTypeCount()
	 */
	@Override
	public int getViewTypeCount() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#hasStableIds()
	 */
	@Override
	public boolean hasStableIds() {
		return false;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return serverList.isEmpty();
	}

	

	/* (non-Javadoc)
	 * @see android.widget.ListAdapter#areAllItemsEnabled()
	 */
	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	/* (non-Javadoc)
	 * @see android.widget.ListAdapter#isEnabled(int)
	 */
	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	public void remove(int position) {
		serverList.remove(position);
	}

	public void add(Server newServer) {
		serverList.add(newServer);
		
	}
	
	public void refresh() {
		for (Server server : serverList) {
			server.queried = false;
		}
		this.notifyDataSetChanged();
	}
	
	public class ServerViewHolder {
		public TextView serverTitle;
		public TextView serverIp;
		public TextView playerCount;
		public TextView serverData;
		public ProgressBar loading;
		
		ServerViewHolder(View serverView) {
			serverTitle = (TextView) serverView.findViewById(R.id.serverTitle);
			serverIp = (TextView) serverView.findViewById(R.id.serverIp);
			playerCount = (TextView) serverView.findViewById(R.id.playerCount);
			serverData = (TextView) serverView.findViewById(R.id.serverData);
			loading = (ProgressBar) serverView.findViewById(R.id.updating_server);
		}
	}

	private class ServerViewUpdater implements ServerDataResultHandler {
		private View view;

		public ServerViewUpdater(View view, Server server) {
			this.view = view;
			new GetServerDataTask(server, this).execute();
		}
		
		@Override
		public void onServerDataResult(Server server, String result) {
			ServerViewHolder holder = (ServerViewHolder) view.getTag();
			
			holder.loading.setVisibility(View.GONE);
			holder.playerCount.setText("" + server.playerCount + "/" + server.maxPlayers);
			
			/*
			 * No Internet = "Network Unreachable"
			 * open port but no server = "The operation timed out"
			 * No open ports = <address> - Connection refused
			 */
			
			holder.serverData.setText(server.motd);

		}
		
		
	}
	
	

}
