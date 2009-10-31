---
title: Sort Objects Your Way
tags: java
---

Just a future reference for myself, if i ever need to sort objects my
way.

    import java.util.Comparator;

    public class IpComparator implements Comparator {

      public int compare(Object host, Object anotherHost) {

          String hostIp = (String)host.toUpperCase();
          String anotherHostIp = (String)anotherHost.toUpperCase();

          return hostIp.compareTo(anotherHostIp);
      }
    }

    Collections.sort(arpTable, new IpComparator());
