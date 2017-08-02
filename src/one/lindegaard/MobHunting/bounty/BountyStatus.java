package one.lindegaard.MobHunting.bounty;

import java.util.HashMap;
import java.util.Map;

public enum BountyStatus {
	open("open", 0), completed("completed", 1), expired("expired", 2), canceled("canceled", 3), deleted("deleted", 4);


	private final static  Map<Integer, BountyStatus> intToBounty = new HashMap<>();


	static {

		for (BountyStatus bountyStatus :
				BountyStatus.values()) {
			intToBounty.put(bountyStatus.getValue(),bountyStatus);
		}



	}


	private String mName;
	private int mStatus;

	BountyStatus(String name, int status) {
		mName = name;
		mStatus = status;
	};

	public int getValue() {
		return mStatus;
	}

	public static BountyStatus valueOf(int value) {
		return intToBounty.get(value);
	}

	public String getName() {
		return mName;
	}

}
