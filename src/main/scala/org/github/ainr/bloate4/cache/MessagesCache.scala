package org.github.ainr.bloate4.cache

import cats.Monad
import cats.implicits.catsSyntaxApplicativeId
import cats.syntax.all._
import com.github.benmanes.caffeine.cache.stats.CacheStats
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import fetch.{Data, DataCache}
import org.github.ainr.bloate4.cache.MessagesCache.CacheConfig

import scala.concurrent.duration.FiniteDuration

trait MessagesCache[F[_]] {
  def make: DataCache[F]
  def stats: F[CacheStats]
}

object MessagesCache {
  final case class CacheConfig(
    expireAfterWrite: FiniteDuration,
    maximumSize: Long
  )
}

final class MessagesScaffeineCache[F[_]: Monad](
  config: CacheConfig
) extends MessagesCache[F] {

  private val cache = Scaffeine()
    .recordStats()
    .expireAfterWrite(config.expireAfterWrite)
    .maximumSize(config.maximumSize)
    .build[Any, Any]()
    .pure[F]

  def buildFromConfig(config: CacheConfig): F[Cache[Any, Any]] = {
    Scaffeine()
      .recordStats()
      .expireAfterWrite(config.expireAfterWrite)
      .maximumSize(config.maximumSize)
      .build[Any, Any]().pure[F]
  }

  def make: DataCache[F] = new DataCache[F] {

    override def lookup[K, V](i: K, data: Data[K, V]): F[Option[V]] = {
      cache
        .map(_.getIfPresent(i).map(v => v.asInstanceOf[V]))
    }

    override def insert[I, A](i: I, v: A, data: Data[I, A]): F[DataCache[F]] = {
      for {
        c <- cache
        _ <- c.put(i, v).pure[F]
        t <- this.asInstanceOf[DataCache[F]].pure[F]
      } yield t
    }
  }

  def stats: F[CacheStats] = cache.map(_.stats())
}

