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

.d2-popup {
    position: fixed;
    z-index: 10;
    top: 0;
    display: inline-block;
    box-sizing: border-box;
    max-width: 90%;
    background: #fafafa;
    opacity: 0;
    border-radius: 0.5em;
    border: 1px solid #c3c3c3;
    box-shadow: 0px 0px 8px rgba(0, 0, 0, 0.3);
    line-height: 0;
    -webkit-transition-property: opacity, -webkit-transform;
    transition-property: opacity, transform;
    -webkit-transition-duration: 0.25s;
    transition-duration: 0.25s;
    -webkit-transition-timing-function: ease;
    transition-timing-function: ease;
    left: 50%;
    transform: translateX(-50%) !important;
}
.d2-popup.is-active {
    -webkit-transform: scale(1) translateZ(0);
    transform: scale(1) translateZ(0);
    opacity: 0.97;
}
.d2-popup.is-scrollable:after {
    content: '';
    position: absolute;
    bottom: 0.3375em;
    left: 0.3375em;
    z-index: 14;
    display: block;
    height: 0.78125em;
    width: 0.625em;
    background-image: url("data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTJweCIgaGVpZ2h0PSIxNXB4IiB2aWV3Qm94PSIwIDAgMTIgMTUiIHZlcnNpb249IjEuMSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgcHJlc2VydmVBc3BlY3RSYXRpbz0ieE1pbllNaW4iPgogICAgPGcgc3Ryb2tlPSJub25lIiBzdHJva2Utd2lkdGg9IjEiIGZpbGw9Im5vbmUiIGZpbGwtcnVsZT0iZXZlbm9kZCI+CiAgICAgICAgPGcgaWQ9IkFycm93IiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgxLjAwMDAwMCwgMS4wMDAwMDApIiBzdHJva2U9ImJsYWNrIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1saW5lY2FwPSJzcXVhcmUiPgogICAgICAgICAgICA8cGF0aCBkPSJNNSwwIEw1LDExLjUiIGlkPSJMaW5lIj48L3BhdGg+CiAgICAgICAgICAgIDxwYXRoIGQ9Ik0wLjUsNy41IEw1LjAyNzY5Mjc5LDEyLjAyNzY5MjgiIGlkPSJMaW5lIj48L3BhdGg+CiAgICAgICAgICAgIDxwYXRoIGQ9Ik00LjUsNy41IEw5LjAyNzY5Mjc5LDEyLjAyNzY5MjgiIGlkPSJMaW5lLTIiIHRyYW5zZm9ybT0idHJhbnNsYXRlKDcuMDAwMDAwLCAxMC4wMDAwMDApIHNjYWxlKC0xLCAxKSB0cmFuc2xhdGUoLTcuMDAwMDAwLCAtMTAuMDAwMDAwKSAiPjwvcGF0aD4KICAgICAgICA8L2c+CiAgICA8L2c+Cjwvc3ZnPgo=");
    background-size: cover;
    opacity: 0.1;
    transition-properties: opacity;
    -webkit-transition-duration: 0.25s;
    transition-duration: 0.25s;
    -webkit-transition-timing-function: ease;
    transition-timing-function: ease;
}
.d2-popup.is-scrollable .d2-popup__wrapper:before, .d2-popup.is-scrollable .d2-popup__wrapper:after {
    content: '';
    position: absolute;
    width: 100%;
    z-index: 12;
    left: 0;
}
.d2-popup.is-scrollable .d2-popup__wrapper:before {
    top: -1px;
    height: 1.1em;
    border-radius: 0.5em 0.5em 0 0;
    background-image: -webkit-linear-gradient(top, #fafafa 50%, rgba(250, 250, 250, 0) 100%);
    background-image: linear-gradient(to bottom, #fafafa 50%, rgba(250, 250, 250, 0) 100%);
}
.d2-popup.is-scrollable .d2-popup__wrapper:after {
    bottom: -1px;
    height: 1.2em;
    border-radius: 0 0 0.5em 0.5em;
    background-image: -webkit-linear-gradient(bottom, #fafafa 50%, rgba(250, 250, 250, 0) 100%);
    background-image: linear-gradient(to top, #fafafa 50%, rgba(250, 250, 250, 0) 100%);
}
.d2-popup.is-scrollable ::-webkit-scrollbar {
    display: none;
}
.d2-popup.is-fully-scrolled:after, .d2-popup.is-fully-scrolled:before {
    opacity: 0;
    -webkit-transition-delay: 0;
    transition-delay: 0;
}

.d2-popup-wrapper {
    position: relative;
    z-index: 14;
    width: 22em;
    display: inline-block;
    box-sizing: inherit;
    overflow: hidden;
    margin: 0;
    background-color: #fafafa;
    border-radius: 0.5em;
    /*line-height: 1;*/
    max-width: 353px;
}
.d2-popup-content {
    position: relative;
    z-index: 8;
    display: inline-block;
    max-height: 15em;
    padding: 1.1em 1.3em 1.2em;
    box-sizing: inherit;
    overflow: auto;
    -webkit-overflow-scrolling: touch;
    background: #fafafa;
    border-radius: 0.5em;
    -webkit-font-smoothing: subpixel-antialiased;
    line-height: normal !important;
}
