package com.enode.common.container;

public interface IObjectContainer {

    <TService> TService resolve(Class<TService> serviceType);

}
