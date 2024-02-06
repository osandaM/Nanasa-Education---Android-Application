package com.example.myapplication

data class AssignmentItem(var assignmentId : String ?= null,var moduleId : String ?= null,var name : String ?= null,var description : String ?= null,
                          var instruction : String ?= null,var year : String ?= null,var month : String ?= null,var date : String ?= null,var hour : String ?= null,
                          var minute : String ?= null)