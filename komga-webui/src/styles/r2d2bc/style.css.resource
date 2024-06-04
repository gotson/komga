/*
 * Copyright 2018-2020 DITA (AM Consulting LLC)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Developed on behalf of: DITA
 * Licensed to: CAST under one or more contributor license agreements.
 */

:root {
    --RS__highlightColor: rgba(255, 255, 0, 0.5);
    --RS__highlightMixBlendMode: multiply;
    --RS__highlightHoverColor: rgba(255, 255, 0, 0.75);
    --RS__underlineBorderColor: rgba(255, 255, 0, 1);
    --RS__underlineHoverColor: rgba(255, 255, 0, 0.1);

    --RS__definitionsColor: rgba(255, 111, 111, 0.5);
    --RS__definitionsMixBlendMode: multiply;
    --RS__definitionsHoverColor: rgba(255, 111, 111, 0.75);
    --RS__definitionsUnderlineBorderColor: rgba(255, 111, 111, 1);
    --RS__definitionsUnderlineHoverColor: rgba(255, 111, 111, 0.1);

    --USER__maxMediaHeight: 95vh;
}

.R2_CLASS_HIGHLIGHT_AREA[data-marker="0"] {
    background-color: var(--RS__highlightColor) !important;
    mix-blend-mode: var(--RS__highlightMixBlendMode) !important;
    z-index: 1;
}
.R2_CLASS_HIGHLIGHT_AREA[data-marker="0"]:hover,
.R2_CLASS_HIGHLIGHT_AREA[data-marker="0"].hover {
    background-color: var(--RS__highlightHoverColor) !important;
}
.R2_CLASS_HIGHLIGHT_AREA[data-marker="1"] {
    border-bottom: 2px solid var(--RS__underlineBorderColor);
    mix-blend-mode: var(--RS__highlightMixBlendMode) !important;
    z-index: 1;
}
.R2_CLASS_HIGHLIGHT_AREA[data-marker="1"]:hover,
.R2_CLASS_HIGHLIGHT_AREA[data-marker="1"].hover {
    background-color: var(--RS__underlineHoverColor) !important;
}

#R2_ID_DEFINITIONS_CONTAINER .R2_CLASS_HIGHLIGHT_AREA[data-marker="0"] {
    background-color: var(--RS__definitionsColor) !important;
    mix-blend-mode: var(--RS__definitionsMixBlendMode) !important;
    z-index: 1;
}
#R2_ID_DEFINITIONS_CONTAINER .R2_CLASS_HIGHLIGHT_AREA[data-marker="0"]:hover,
#R2_ID_DEFINITIONS_CONTAINER .R2_CLASS_HIGHLIGHT_AREA[data-marker="0"].hover {
    background-color: var(--RS__definitionsHoverColor) !important;
}
#R2_ID_DEFINITIONS_CONTAINER .R2_CLASS_HIGHLIGHT_AREA[data-marker="1"] {
    border-bottom: 2px solid var(--RS__definitionsUnderlineBorderColor);
    mix-blend-mode: var(--RS__definitionsMixBlendMode) !important;
    z-index: 1;
}
#R2_ID_DEFINITIONS_CONTAINER .R2_CLASS_HIGHLIGHT_AREA[data-marker="1"]:hover,
#R2_ID_DEFINITIONS_CONTAINER .R2_CLASS_HIGHLIGHT_AREA[data-marker="1"].hover {
    background-color: var(--RS__definitionsUnderlineHoverColor) !important;
}

:root[style] .r2-mo-active,
:root .r2-mo-active {
    background-color: yellow !important;
    color: black !important;
}
:root[style*="readium-night-on"] .r2-mo-active {
    background-color: #333333 !important;
    color: white !important;
}
:root[style*="readium-sepia-on"] .r2-mo-active {
    background-color: silver !important;
    color: black !important;
}

.orange {
    background: rgba(255, 165, 0, 0.5) !important;
    border-bottom: solid 2px rgb(255, 165, 0) !important;
    z-index: 2;
    position: relative;
}
.red {
    background: rgba(255,0,0,0.5) !important;
    border-bottom: solid 2px rgb(255,0,0) !important;
    z-index: 2;
    position: relative;
}
.blue {
    background: rgba(0,0,255,0.5) !important;
    border-bottom: solid 2px rgb(0,0,255) !important;
    z-index: 2;
    position: relative;
}
.purple {
    background: rgba(102,0,153,0.5) !important;
    border-bottom: solid 2px rgb(102,0,153) !important;
    z-index: 2;
    position: relative;
}
.green {
    background: rgba(0,102,0,0.5) !important;
    border-bottom: solid 2px rgb(0,102,0) !important;
    z-index: 2;
    position: relative;
}
.gray {
    background: rgba(85,85,85,0.5) !important;
    border-bottom: solid 2px rgb(85,85,85) !important;
    z-index: 2;
    position: relative;
}
.orangetext {
    color: rgb(255, 165, 0) !important;
}
.redtext {
    color: rgb(255,0,0) !important;
}
.bluetext {
    color: rgb(0,0,255) !important;
}
.purpletext {
    color: rgb(102,0,153) !important;
}
.greentext {
    color: rgb(0,102,0) !important;
}
.graytext {
    color:  rgb(85,85,85) !important;
}
[data-tts-current-word="true"][data-tts-color="orange"] {
    background: rgba(255, 165, 0, 0.5) !important;
    border-bottom: solid 2px rgb(255, 165, 0) !important;
    z-index: 2;
    position: relative;
}
[data-tts-current-line="true"][data-tts-color="orange"] {
    background: rgba(255, 165, 0, 0.5) !important;
    z-index: 2;
    position: relative;
}


[data-tts-current-word="true"][data-tts-color="red"] {
    background: rgba(255,0,0,0.5) !important;
    border-bottom: solid 2px rgba(255,0,0) !important;
    z-index: 2;
    position: relative;
}
[data-tts-current-line="true"][data-tts-color="red"] {
    background: rgba(255,0,0,0.5) !important;
    z-index: 2;
    position: relative;
}

[data-tts-current-word="true"][data-tts-color="blue"] {
    background: rgba(0,0,255,0.5) !important;
    border-bottom: solid 2px rgba(0,0,255) !important;
    z-index: 2;
    position: relative;
}
[data-tts-current-line="true"][data-tts-color="blue"] {
    background: rgba(0,0,255,0.5) !important;
    z-index: 2;
    position: relative;
}

[data-tts-current-word="true"][data-tts-color="purple"] {
    background: rgba(102,0,153,0.5) !important;
    border-bottom: solid 2px rgba(102,0,153) !important;
    z-index: 2;
    position: relative;
}
[data-tts-current-line="true"][data-tts-color="purple"] {
    background: rgba(102,0,153,0.5) !important;
    z-index: 2;
    position: relative;
}

[data-tts-current-word="true"][data-tts-color="green"] {
    background: rgba(0,102,0,0.5) !important;
    border-bottom: solid 2px rgba(0,102,0) !important;
    z-index: 2;
    position: relative;
}
[data-tts-current-line="true"][data-tts-color="green"] {
    background: rgba(0,102,0,0.5) !important;
    z-index: 2;
    position: relative;
}

[data-tts-current-word="true"][data-tts-color="gray"] {
    background: rgba(85,85,85,0.5) !important;
    border-bottom: solid 2px rgba(85,85,85) !important;
    z-index: 2;
    position: relative;
}
[data-tts-current-line="true"][data-tts-color="gray"] {
    background: rgba(85,85,85,0.5) !important;
    z-index: 2;
    position: relative;
}

.icon:hover .icon-tooltip {
    display: block;
}

.icon-tooltip {
    display: none;
    position: absolute;
    left: 50%;
    /*top: 50%;*/
    transform: translate(-50%, -105%);
    background: #DADADA;
    padding: 0.25rem 0.5rem;
    text-overflow: ellipsis;
    max-width: 22rem;
    min-width: 100px;
    z-index: 1000;
    border: 1px dotted #5b5852;
}

.icon-tooltip:before, .icon-tooltip:after {
    content: '';
    display: block;
}

.icon-tooltip:before {
    content: " ";
    display: block;
    position: absolute;
    top: 100%;
    left: 50%;
    width: 0;
    height: 0;
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
    border-top: 5px solid #5b5852;
    margin-left: -10px;
}

img {
    max-height: var(--USER__maxMediaHeight) !important;
}

#R2_ID_GUTTER_RIGHT_CONTAINER {
    position: absolute;
    top: 0;
    right: -20px;
}
