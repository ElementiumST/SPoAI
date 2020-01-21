package com.example;

import org.bukkit.metadata.MetadataValue;

public interface SummonUnitStatus extends MetadataValue {
    String SLAVE = "isSlave";
    String MASTER = "isMaster";

    void call(SummonUnitStatus.OnSummonActionCompleteListener onSummonActionCompleteListener);
    void remove(SummonUnitStatus.OnSummonActionCompleteListener onSummonActionCompleteListener);
    boolean unCall(SummonUnitStatus.OnSummonActionCompleteListener onSummonActionCompleteListener);
    void onSlaveDeath();
    void onMasterDeath();
    void executeOrder(Order order, Object[] args);

    interface OnSummonActionCompleteListener {
        void onSuccess(String message);
        void onFail(String message);
    }
}