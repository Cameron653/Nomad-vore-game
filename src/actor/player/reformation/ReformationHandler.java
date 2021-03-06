package actor.player.reformation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReformationHandler {

	private long nextUid;
	private List<MachineEntry> machines;

	public ReformationHandler() {
		nextUid = 0;
		machines = new ArrayList<MachineEntry>();
	}

	public ReformationHandler(DataInputStream dstream) throws IOException {
		nextUid = dstream.readLong();
		machines = new ArrayList<MachineEntry>();
		int c = dstream.readInt();
		for (int i = 0; i < c; i++) {
			machines.add(new MachineEntry(dstream));
		}
	}

	public void save(DataOutputStream dstream) throws IOException {
		dstream.writeLong(nextUid);
		dstream.writeInt(machines.size());
		for (int i = 0; i < machines.size(); i++) {
			machines.get(i).save(dstream);
		}
	}

	public Long linkMachine(String zone)
	{
		machines.add(new MachineEntry(zone,nextUid));
		return nextUid++;
	}

	public void unLinkMachine(String name) {
		for (int i = 0; i < machines.size(); i++) {
			if (machines.get(i).getZoneName().equals(name)) {
				machines.remove(i);
				return;
			}
		}
	}

	public void clear() {
		machines.clear();
	}

	public List<MachineEntry> getMachines() {
		return machines;
	}


}
