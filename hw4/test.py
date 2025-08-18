import hazelcast

client = hazelcast.HazelcastClient(
    cluster_name="dev",  # Make sure this matches your hazelcast.xml
    cluster_members=["10.158.82.87:29000"],
    lifecycle_listeners=[lambda state: print("Lifecycle event >>>", state)],
)

my_map = client.get_map("test-map").blocking()
my_map.put("hello", "world")
value = my_map.get("hello")
print("Value from map:", value)

client.shutdown()
