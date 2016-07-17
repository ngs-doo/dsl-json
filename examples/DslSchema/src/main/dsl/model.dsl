module example {
	struct Model {
		String string; //non null string
		List<int> integers; //no null elements allowed
		Set<decimal?> decimals; //null elements allowed
		UUID[] uuids;
		Vector<long>? longs; //property can be null, elements can't
		int number;
		List<Nested?> nested; //property can't be null elements can
		Abstract abs; // reference to interface
		List<State> states;
		Date date; // non nullable java or joda date
		List<Date?> dates;
	}
	struct Nested {
		long x;
		double y;
		float z;
	}
	mixin Abstract {
		int x;
	}
	struct Concrete {
		int y;
		has mixin Abstract;
	}
	enum State {
		LOW;
		MID;
		HI;
	}
}