package com.leomorev.surreel.util;

import net.minecraftforge.energy.EnergyStorage;

public abstract class Energy extends EnergyStorage {
    public Energy(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    @Override
    public int extractEnergy(int max, boolean sim){
        int extEnergy = super.extractEnergy(max, sim);
        if(extEnergy != 0){onEnergyChanged();}
        return extEnergy;
    }

    @Override
    public int receiveEnergy(int max, boolean sim) {
        int recEnergy = super.receiveEnergy(max, sim);
        if(recEnergy != 0){onEnergyChanged();}
        return recEnergy;
    }

    public int setEnergy(int energy){
        this.energy = energy;
        return energy;
    }

    public abstract void onEnergyChanged();
}
