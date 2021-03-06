/*
 * This file is part of alf.io.
 *
 * alf.io is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * alf.io is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with alf.io.  If not, see <http://www.gnu.org/licenses/>.
 */

package alfio.pi.repository

import alfio.pi.model.Authority
import alfio.pi.model.Event
import alfio.pi.model.Role
import alfio.pi.model.User
import ch.digitalfondue.npjt.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

@QueryRepository
interface UserRepository {

    @Query("insert into user(username, password) values(:username, :password)")
    fun insert(@Bind("username") username: String, @Bind("password") password: String): AffectedRowCountAndKey<Int>

    @Query("select id, username from user where username = :username")
    fun findByUsername(@Bind("username") username: String): Optional<User>

    @Query("select id, username from user where id = :userId")
    fun findById(@Bind("userId") id: Int): Optional<User>

    @Query("select id, username from user")
    fun findAll(): List<User>

    @Query("select user.id, user.username from user, authority where user.username = authority.username and authority.role <> 'ADMIN'")
    fun findAllOperators(): List<User>

    @Query("update user set password = :password where id = :userId")
    fun updatePassword(@Bind("userId") id: Int, @Bind("password") password: String)

}

@QueryRepository
interface AuthorityRepository {
    @Query("insert into authority(username, role) values(:username, :role)")
    fun insert(@Bind("username") username: String, @Bind("role") role: Role): Int

    @Query("select * from authority where username = :username")
    fun findByUsername(@Bind("username") username: String): List<Authority>
}

@QueryRepository
interface EventRepository {

    @Query("select * from event")
    fun loadAll(): List<Event>

    @Query("select * from event where id = :eventId")
    fun loadSingle(@Bind("eventId") eventId: Int): Optional<Event>

    @Query("select * from event where key = :key")
    fun loadSingle(@Bind("key") eventName: String): Optional<Event>

    @Query(INSERT_QUERY)
    fun insert(@Bind("key") key: String,
               @Bind("name") name: String,
               @Bind("imageUrl") imageUrl: String?,
               @Bind("begin") begin: LocalDateTime,
               @Bind("end") end: LocalDateTime,
               @Bind("location") location: String?,
               @Bind("apiVersion") apiVersion: Int,
               @Bind("oneDay") oneDay: Boolean,
               @Bind("active") active: Boolean): AffectedRowCountAndKey<Int>

    @Query("update event set last_update = :timestamp where key = :key")
    fun updateTimestamp(@Bind("key") key: String, @Bind("timestamp") lastUpdate: ZonedDateTime): Int

    @Query(UPDATE_QUERY)
    fun update(@Bind("key") key: String,
               @Bind("name") name: String,
               @Bind("imageUrl") imageUrl: String?,
               @Bind("begin") begin: LocalDateTime,
               @Bind("end") end: LocalDateTime,
               @Bind("location") location: String?,
               @Bind("apiVersion") apiVersion: Int,
               @Bind("oneDay") oneDay: Boolean,
               @Bind("active") active: Boolean,
               @Bind("id") id: Int)

    @Query(type = QueryType.TEMPLATE, value = INSERT_QUERY)
    fun bulkInsert(): String

    @Query(type = QueryType.TEMPLATE, value = UPDATE_QUERY)
    fun bulkUpdate(): String

    @Query("update event set active = :state where id = :id")
    fun toggleActivation(@Bind("id") id: Int, @Bind("state") state: Boolean): Int

    companion object {
        private const val INSERT_QUERY = "insert into event(key, name, image_url, begin_ts, end_ts, location, api_version, one_day, active) values(:key, :name, :imageUrl, :begin, :end, :location, :apiVersion, :oneDay, :active)"
        private const val UPDATE_QUERY = "update event set key = :key, name = :name, image_url = :imageUrl, begin_ts = :begin, end_ts = :end, location = :location, api_version = :apiVersion, one_day = :oneDay where id = :id"
    }
}

@QueryRepository
interface EventDataRepository {
    @Query(type = QueryType.TEMPLATE, value = "update event_data set data = ?, last_update = ? where key = ?")
    fun updateTemplate(): String

    @Query(type = QueryType.TEMPLATE, value = "insert into event_data (data, last_update, key) values(?, ?, ?)")
    fun insertTemplate(): String

    @Query("select count(key) from event_data where key = :key")
    fun isPresent(@Bind("key") key: String): Int

    @Query("select key from event_data")
    fun loadAllKeys(): List<String>

    @Query(type = QueryType.TEMPLATE, value = "select data from event_data where key = :key")
    fun loadEventDataTemplate(): String
}