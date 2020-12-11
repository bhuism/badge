#!/bin/bash

# Copyright © 2017 Google Inc.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

IFS=$'\n\t'
set -eou pipefail

gcloud config set account bas@appsource.nl

for project in badge citaten citaten-client ; do

	IMAGE=eu.gcr.io/badge-260212/$project

	C=0
	for digest in $(gcloud container images list-tags $IMAGE --limit=unlimited --sort-by=~TIMESTAMP \
		--filter="NOT tags:('latest')" --format='get(digest)'); do
		(
			set -x
			gcloud container images delete -q --force-delete-tags "${IMAGE}@${digest}"
		)
		let C=C+1
	done

	echo "Deleted ${C} images in ${IMAGE}." >&2


done
