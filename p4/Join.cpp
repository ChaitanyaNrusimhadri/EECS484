#include "Join.hpp"

#include <unordered_map>
#include <vector>

using namespace std;

/*
 * Input: Disk, Memory, Disk page ids for left relation, Disk page ids for right relation
 * Output: Vector of Buckets of size (MEM_SIZE_IN_PAGE - 1) after partition
 */
vector<Bucket> partition(Disk* disk, Mem* mem, pair<uint, uint> left_rel, pair<uint, uint> right_rel) {
	// TODO: implement partition phase

	//produce k disk-based partitions
	vector<Bucket> partitions(MEM_SIZE_IN_PAGE - 1, Bucket(disk)); // placeholder
	uint num_buckets = MEM_SIZE_IN_PAGE - 1;

	//loadRecord missing

	// Hash Relation R --> Left Relation
	for (uint i = left_rel.first; i < left_rel.second; i++) {
		mem->loadFromDisk(disk, i, num_buckets);

		//hash each record
		for (uint j = 0; j < mem->mem_page(num_buckets)->size(); j++) {
			int hash_value = mem->mem_page(num_buckets)->get_record(j).partition_hash() % num_buckets;

			Page* hash_page = mem->mem_page(hash_value);
			//hash_page->loadRecord(mem->mem_page(num_buckets)->get_record(j));

			// If the memory page is full, flush the memory page to the disk and add it to the appropriate partition
			if (hash_page->full()) {
				uint new_disk_page_id = mem->flushToDisk(disk, hash_value);
				partitions[hash_value].add_left_rel_page(new_disk_page_id);
				hash_page->reset(); //added line to reset hash_page to empty
			}

			hash_page->loadRecord(mem->mem_page(num_buckets)->get_record(j));
		}
	}

	//don't input throught input page, i = 0 or 1?
	for (uint i = 0; i < num_buckets; i++) {
		//mem->mem_page(i)->full()
		if (!mem->mem_page(i)->empty() && num_buckets != i) {
			uint new_disk_page_id = mem->flushToDisk(disk, i);
			partitions[i].add_left_rel_page(new_disk_page_id);
		}
	}

	mem->reset();

	// Hash Relation S --> Right Relation
	for (uint i = right_rel.first; i < right_rel.second; i++) {
		mem->loadFromDisk(disk, i, num_buckets);

		for (uint j = 0; j < mem->mem_page(num_buckets)->size(); j++) {
			int hash_value = mem->mem_page(num_buckets)->get_record(j).partition_hash() % num_buckets;

			Page* hash_page = mem->mem_page(hash_value);
			//hash_page->loadRecord(mem->mem_page(num_buckets)->get_record(j));

			// If the memory page is full, flush the memory page to the disk and add it to the appropriate partition
			if (hash_page->full()) {
				uint new_disk_page_id = mem->flushToDisk(disk, hash_value);
				partitions[hash_value].add_right_rel_page(new_disk_page_id);
				hash_page->reset(); //added line to reset hash_page to empty
			}

			hash_page->loadRecord(mem->mem_page(num_buckets)->get_record(j));
		}
	}

	for (uint i = 0; i < num_buckets; i++) {
		//mem->mem_page(i)->full()
		if (!mem->mem_page(i)->empty() && num_buckets != i) {
			uint new_disk_page_id = mem->flushToDisk(disk, i);
			partitions[i].add_right_rel_page(new_disk_page_id);
		}
	}

	mem->reset();

	return partitions;
}

/*
 * Input: Disk, Memory, Vector of Buckets after partition
 * Output: Vector of disk page ids for join result
 */
vector<uint> probe(Disk* disk, Mem* mem, vector<Bucket>& partitions) {
	// TODO: implement probe phase
	vector<uint> disk_pages; // placeholder
	//Perform Natural Join Rk ⋈ Sk

	//for loop over each partition
	//sum num_records of each relation
	//R smaller
	int left_rel_sum = 0, right_rel_sum = 0;
	for (auto& bucket : partitions) {
		left_rel_sum += bucket.num_left_rel_record;
		right_rel_sum += bucket.num_right_rel_record;
	}

	// Iterating over partitions
	//mem_page(0) for input page, mem_page(1) for output page
	for (auto& bucket : partitions) {
		vector<uint> left_rel, right_rel;

		//find smaller relation and put it in left_rel
		if (left_rel_sum <= right_rel_sum) {
			left_rel = bucket.get_left_rel();
			right_rel = bucket.get_right_rel();
		} else {
			left_rel = bucket.get_right_rel();
			right_rel = bucket.get_left_rel();
		}

		//unordered_map<int, vector<Record>> subPartitions;
		for (auto& left_page_id : left_rel) {
			mem->loadFromDisk(disk, left_page_id, 0);
			for (uint i = 0; i < mem->mem_page(0)->size(); i++) {
				int hash_value = mem->mem_page(0)->get_record(i).probe_hash() % (MEM_SIZE_IN_PAGE - 2) + 2;
				Page* hash_page = mem->mem_page(hash_value);
				hash_page->loadRecord(mem->mem_page(0)->get_record(i));
			}
		}

		for (auto& right_page_id : right_rel) {
			mem->loadFromDisk(disk, right_page_id, 0);
			for (uint i = 0; i < mem->mem_page(0)->size(); i++) {
				int hash_value = mem->mem_page(0)->get_record(i).probe_hash() % (MEM_SIZE_IN_PAGE - 2) + 2;

				// Match the records of each partition of the other relation.

				Page* hash_page = mem->mem_page(hash_value);
				for (uint j = 0; j < hash_page->size(); j++) {
					Record hash_page_record = hash_page->get_record(j);
					Page* output_page = mem->mem_page(1);

					//SUS PART
					if (hash_page_record == mem->mem_page(0)->get_record(i)) {
						if (output_page->full()) {
							uint new_disk_page_id = mem->flushToDisk(disk, 1);
							disk_pages.push_back(new_disk_page_id);
							output_page->reset();
						}

						output_page->loadPair(hash_page->get_record(j), mem->mem_page(0)->get_record(i));
					}
					//if () { //r.p = s.o
					//}
				}
				/*
					for (auto& left_rec : mem->mem_page(hash_value)->get_record(i)) {
						if (left_rec == mem->mem_page(hash_value)->get_record(i)) { //change 1 to hash value
							if (mem->mem_page(1)->full()) {
								uint new_disk_page_id = mem->flushToDisk(disk, 1);
								disk_pages.push_back(new_disk_page_id);
								//mem->mem_page(0)->reset();
							}
							mem->mem_page(1)->loadPair(left_rec, mem->mem_page(0)->get_record(i));
						}
					}
					*/
			}
		}

		for (uint i = 2; i < MEM_SIZE_IN_PAGE; i++)
			mem->mem_page(i)->reset();
	}

	// Check if there are still records left in memory to write to the disk.
	if (!mem->mem_page(1)->empty()) {
		uint new_disk_page_id = mem->flushToDisk(disk, 1);
		disk_pages.push_back(new_disk_page_id);
	}

	return disk_pages;

	/*
	for (uint i = 0; i < partitions.size(); i++) {
		Bucket bucket = partitions[i];
		vector<uint> left_rel = bucket.get_left_rel();
		vector<uint> right_rel = bucket.get_right_rel();

		//foe each tuple r

		for (auto& left_page_id : left_rel) { //iterate through every page id of list of page ids
			mem->loadFromDisk(disk, left_page_id, 0); //change 0 to num_buckets?

			for (uint i = 0; i < mem->mem_page(0)->size(); i++) {
				// Put r in bucket h2(r.p)
				int hash_value = mem->mem_page(0)->get_record(i).probe_hash() % partitions.size();

				//hashing and bucketing process for relation R
				//disk_pages[hash_value].
			}
		}

		//for each tuple s

		for (auto& right_page_id : right_rel) {
			mem->loadFromDisk(disk, right_page_id, 0);

			for (uint j = 0; j < mem->mem_page(1)->size(); j++) {
				// Put s in bucket h2(s.σ)
				int hash_value = mem->mem_page(1)->get_record(j).probe_hash() % partitions.size();
			}


		}
		*/
}
