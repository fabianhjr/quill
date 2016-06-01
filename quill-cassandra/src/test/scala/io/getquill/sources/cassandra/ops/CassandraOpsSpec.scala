package io.getquill.context.cassandra.ops

import io.getquill._
import io.getquill.context.cassandra.mirrorContext

class CassandraOpsSpec extends Spec {

  import mirrorContext._

  "query" - {
    "allowFiltering" in {
      val q = quote {
        query[TestEntity].filter(t => t.i > 10).allowFiltering
      }
      mirrorContext.run(q).cql mustEqual
        "SELECT s, i, l, o FROM TestEntity WHERE i > 10 ALLOW FILTERING"
    }
  }

  "insert" - {
    "ifNotExists" in {
      val q = quote {
        query[TestEntity].insert(_.s -> "s").ifNotExists
      }
      mirrorContext.run(q).cql mustEqual
        "INSERT INTO TestEntity (s) VALUES ('s') IF NOT EXISTS"
    }
    "options" - {
      "timestamp" in {
        val q = quote {
          query[TestEntity].insert(_.s -> "s").usingTimestamp(1)
        }
        mirrorContext.run(q).cql mustEqual
          "INSERT INTO TestEntity (s) VALUES ('s') USING TIMESTAMP 1"
      }
      "ttl" in {
        val q = quote {
          query[TestEntity].insert(_.s -> "s").usingTtl(1)
        }
        mirrorContext.run(q).cql mustEqual
          "INSERT INTO TestEntity (s) VALUES ('s') USING TTL 1"
      }
      "both" in {
        val q = quote {
          query[TestEntity].insert(_.s -> "s").using(1, 2)
        }
        mirrorContext.run(q).cql mustEqual
          "INSERT INTO TestEntity (s) VALUES ('s') USING TIMESTAMP 1 AND TTL 2"
      }
    }
  }

  "update" - {
    "options" - {
      "timestamp" in {
        val q = quote {
          query[TestEntity].usingTimestamp(99).update
        }
        mirrorContext.run(q)(List()).cql mustEqual
          "UPDATE TestEntity USING TIMESTAMP 99 SET s = ?, i = ?, l = ?, o = ?"
      }
      "ttl" in {
        val q = quote {
          query[TestEntity].usingTtl(1).update
        }
        mirrorContext.run(q)(List()).cql mustEqual
          "UPDATE TestEntity USING TTL 1 SET s = ?, i = ?, l = ?, o = ?"
      }
      "both" in {
        val q = quote {
          query[TestEntity].using(1, 2).update
        }
        mirrorContext.run(q)(List()).cql mustEqual
          "UPDATE TestEntity USING TIMESTAMP 1 AND TTL 2 SET s = ?, i = ?, l = ?, o = ?"
      }
    }
    "if" in {

      val q = quote {
        query[TestEntity].update(t => t.s -> "b").ifCond(t => t.s == "a")
      }
      mirrorContext.run(q).cql mustEqual
        "UPDATE TestEntity SET s = 'b' IF s = 'a'"
    }
  }

  "delete" - {
    "column" in {
      val q = quote {
        query[TestEntity].filter(t => t.i == 1).map(t => t.i).delete
      }
      mirrorContext.run(q).cql mustEqual
        "DELETE i FROM TestEntity WHERE i = 1"
    }
    "options" - {
      "timestamp" in {
        val q = quote {
          query[TestEntity].usingTimestamp(9).filter(t => t.i == 1).delete
        }
        mirrorContext.run(q).cql mustEqual
          "DELETE FROM TestEntity USING TIMESTAMP 9 WHERE i = 1"
      }
      "ttl" in {
        val q = quote {
          query[TestEntity].usingTtl(9).filter(t => t.i == 1).delete
        }
        mirrorContext.run(q).cql mustEqual
          "DELETE FROM TestEntity USING TTL 9 WHERE i = 1"
      }
      "both" in {
        val q = quote {
          query[TestEntity].using(ts = 9, ttl = 10).filter(t => t.i == 1).delete
        }
        mirrorContext.run(q).cql mustEqual
          "DELETE FROM TestEntity USING TIMESTAMP 9 AND TTL 10 WHERE i = 1"
      }
    }
    "if" in {
      val q = quote {
        query[TestEntity].filter(t => t.i == 1).delete.ifCond(t => t.s == "s")
      }
      mirrorContext.run(q).cql mustEqual
        "DELETE FROM TestEntity WHERE i = 1 IF s = 's'"
    }
    "ifExists" in {
      val q = quote {
        query[TestEntity].delete.ifExists
      }
      mirrorContext.run(q).cql mustEqual
        "TRUNCATE TestEntity IF EXISTS"
    }
  }
}
