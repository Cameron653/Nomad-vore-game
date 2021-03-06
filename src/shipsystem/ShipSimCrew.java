package shipsystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.w3c.dom.Element;

import actor.npc.Crew;

public class ShipSimCrew extends ShipAbility {
	
	private Crew crew;
	
	public ShipSimCrew(Element node, String name) {
		abilityType=AbilityType.SA_CREW;
		crew=new Crew(node);
	}
	
	public ShipSimCrew(DataInputStream dstream, String m_name) throws IOException {
		abilityType = AbilityType.SA_CREW;
		crew=new Crew(dstream);
	}
	
	@Override
	public void save(DataOutputStream dstream) throws IOException {
		crew.save(dstream);
	}

	public Crew getCrew() {
		return crew;
	}

}
