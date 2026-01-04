import java.util.*;
import java.util.concurrent.*;

// Vehicle class
class Vehicle {
    enum Type { CAR, TRUCK, MOTORCYCLE }
    private final String plate;
    private final Type type;

    public Vehicle(String plate, Type type) {
        this.plate = plate;
        this.type = type;
    }

    public String getPlate() { return plate; }
    public Type getType() { return type; }

    @Override
    public String toString() {
        return type + "(" + plate + ")";
    }
}

// Parking Lot
class ParkingLot {
    private final int capacity;
    private final Set<String> parkedVehicles = ConcurrentHashMap.newKeySet();

    public ParkingLot(int capacity) {
        this.capacity = capacity;
    }

    public synchronized boolean enter(Vehicle v) {
        if (parkedVehicles.size() < capacity) {
            parkedVehicles.add(v.getPlate());
            System.out.println(v + " entered. Spots left: " + (capacity - parkedVehicles.size()));
            return true;
        } else {
            System.out.println(v + " denied entry (lot full).");
            return false;
        }
    }

    public synchronized void exit(Vehicle v) {
        if (parkedVehicles.remove(v.getPlate())) {
            System.out.println(v + " exited. Spots left: " + (capacity - parkedVehicles.size()));
        }
    }
}

// Entry Gate Thread
class EntryGate implements Runnable {
    private final ParkingLot lot;
    private final String name;
    private final BlockingQueue<Vehicle> arrivals;

    public EntryGate(String name, ParkingLot lot, BlockingQueue<Vehicle> arrivals) {
        this.name = name;
        this.lot = lot;
        this.arrivals = arrivals;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Vehicle v = arrivals.take();
                System.out.println("[" + name + "] Processing " + v);
                lot.enter(v);
            }
        } catch (InterruptedException e) {
            System.out.println("[" + name + "] stopped.");
        }
    }
}

// Exit Gate Thread
class ExitGate implements Runnable {
    private final ParkingLot lot;
    private final String name;
    private final BlockingQueue<Vehicle> departures;

    public ExitGate(String name, ParkingLot lot, BlockingQueue<Vehicle> departures) {
        this.name = name;
        this.lot = lot;
        this.departures = departures;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Vehicle v = departures.take();
                System.out.println("[" + name + "] Processing exit of " + v);
                lot.exit(v);
            }
        } catch (InterruptedException e) {
            System.out.println("[" + name + "] stopped.");
        }
    }
}

// Simulator
class ParkingSimulator {
    public static void main(String[] args) throws InterruptedException {
        ParkingLot lot = new ParkingLot(5); // capacity 5
        BlockingQueue<Vehicle> arrivals = new LinkedBlockingQueue<>();
        BlockingQueue<Vehicle> departures = new LinkedBlockingQueue<>();

        Thread entry1 = new Thread(new EntryGate("Entry-1", lot, arrivals));
        Thread entry2 = new Thread(new EntryGate("Entry-2", lot, arrivals));
        Thread exit1 = new Thread(new ExitGate("Exit-1", lot, departures));
        Thread exit2 = new Thread(new ExitGate("Exit-2", lot, departures));

        entry1.start();
        entry2.start();
        exit1.start();
        exit2.start();

        // Generate vehicles
        for (int i = 0; i < 10; i++) {
            Vehicle v = new Vehicle("PLK-" + i, Vehicle.Type.values()[i % 3]);
            arrivals.put(v);
            Thread.sleep(500);
            if (i % 2 == 0) departures.put(v); // some leave
        }

        Thread.sleep(5000);
        entry1.interrupt();
        entry2.interrupt();
        exit1.interrupt();
        exit2.interrupt();
    }
}








