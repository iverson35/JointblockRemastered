package jp.mc.ancientred.jointblock.api;

import java.util.Map;

public interface IJBActionFactory {
    IJBModelItem.IJBActionHandler createAction(String name, Map<String, Object> args);
}
