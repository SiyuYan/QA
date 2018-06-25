.. _license:

########
Licenses
########

Developer motivations
=====================

We try as much as possible to distribute Gatling and its side projects under Apache License v2.0.
As much as possible means that we sometimes have to cope with some dependencies that are not Apache License v2.0 compatible or even not open source friendly.

Copyright of the source code and the Gatling logos belongs to `eBusiness Information <http://www.ebusinessinformation.fr>`_, an `Excilys Group <http://www.excilys.com>`_ company.

Main Gatling project
====================

Gatling is licensed under the Apache License v2.0 available here: `HTML <http://www.apache.org/licenses/LICENSE-2.0.html>`_ | `TXT <http://www.apache.org/licenses/LICENSE-2.0.txt>`_

Highcharts based modules
========================

The default report generation module makes use of Highcharts and Highstock, two great javascript libraries developed by Highsoft. These libraries are neither open source, nor free for our use case.

Our sponsor, eBusiness Information purchased developer licenses, but as a consequence, this module cannot be open sourced and has to be developed in a separated project : `gatling-highcharts <https://github.com/gatling/gatling-highcharts>`_.
However, for convenience reasons, we distribute a ready to use bundle.

Making it short, we distribute this module for free with the following restrictions :

* one can not edit the source code without buying a Highcharts license for working on Gatling
* the shipped Highcharts or Highstock copies cannot be used outside of Gatling standard usage

For more information, please refer to the `gatling-highcharts license <https://github.com/gatling/gatling-highcharts/blob/1.5.X/gatling-charts-highcharts/src/main/resources/META-INF/LICENCE>`_ or feel free to `ask <https://groups.google.com/forum/#!forum/gatling>`_.

If Highsoft happens to change its licensing policy in a more open-source friendly manner, we'd gladly change our own.
