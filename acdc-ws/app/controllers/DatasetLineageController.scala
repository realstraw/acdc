/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package controllers

import javax.inject._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import play.api.libs.json.{JsError, Json}
import play.api.mvc._

import com.salesforce.mce.acdc.db.DatasetLineageQuery
import services.DatabaseService
import utils.{AuthTransformAction, InvalidApiRequest, ValidApiRequest}

@Singleton
class DatasetLineageController @Inject() (
  cc: ControllerComponents,
  dbService: DatabaseService,
  authAction: AuthTransformAction
)(implicit
  ec: ExecutionContext
) extends AcdcAbstractController(cc, dbService) {

  def setSources(dest: String) = authAction.async(parse.json) { request =>
    request match {
      case ValidApiRequest(apiRole, req) =>
        req.body
          .validate[Seq[String]]
          .fold(
            e => Future.successful(BadRequest(JsError.toJson(e))),
            rs =>
              db.async(DatasetLineageQuery.ForDestination(dest).setSources(rs))
                .map(r => Created(Json.toJson(r.getOrElse(0))))
          )
      case InvalidApiRequest(_) => Future.successful(Results.Unauthorized)
    }
  }

  def getSources(dest: String) = authAction.async { request =>
    request match {
      case ValidApiRequest(apiRole, _) =>
        db.async(DatasetLineageQuery.ForDestination(dest).getSources())
          .map(rs => Ok(Json.toJson(rs)))
      case InvalidApiRequest(_) => Future.successful(Results.Unauthorized)
    }
  }

  def delete(dest: String) = authAction.async { request =>
    request match {
      case ValidApiRequest(apiRole, _) =>
        db.async(DatasetLineageQuery.ForDestination(dest).delete()).map(r => Ok(Json.toJson(r)))
      case InvalidApiRequest(_) => Future.successful(Results.Unauthorized)
    }
  }

}
