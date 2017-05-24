package com.schibstedspain.leku;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class NullView {

  @SuppressWarnings("unchecked")
  public static <T> T createFor(Class<T> viewClass) {
    InvocationHandler emptyHandler = (proxy, method, args) -> null;
    ClassLoader classLoader = viewClass.getClassLoader();
    Class[] interfaces = new Class[] { viewClass };
    return (T) Proxy.newProxyInstance(classLoader, interfaces, emptyHandler);
  }
}